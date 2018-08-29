package mvp.actors

import akka.actor.Actor
import akka.util.ByteString
import mvp.cli.ConsoleActor.{BlockchainRequest, HeadersRequest, UserMessageFromCLI}
import com.typesafe.scalalogging.StrictLogging
import mvp.data.{Blockchain, Modifier, State, _}
import io.circe.syntax._
import io.circe.generic.auto._
import mvp.MVP.settings
import mvp.actors.Messages._
import mvp.local.messageHolder.UserMessage._
import mvp.actors.ModifiersHolder.RequestModifiers
import mvp.local.messageHolder.UserMessage
import mvp.local.{Generator, Keys}
import mvp.utils.Crypto.Sha256RipeMD160
import mvp.utils.BlockchainUtils.{randomByteString, toByteString, base16Encode}
import mvp.utils.EncodingUtils._
import scorex.crypto.signatures.Curve25519

class StateHolder extends Actor with StrictLogging {
  var blockChain: Blockchain = Blockchain.recoverBlockchain
  var state: State = State.recoverState
  val keys: Keys = Keys.recoverKeys
  var messagesHolder: Seq[UserMessage] = Seq.empty
  var currentSalt: ByteString = randomByteString

  override def receive: Receive = {
    case Headers(headers: Seq[Header]) => headers.filter(validate).foreach(add)
    case InfoMessage(msg: UserMessage) => addMessageAndCreateTx(msg).foreach(tx => self ! Transactions(Seq(tx)))
    case Payloads(payloads: Seq[Payload]) => payloads.filter(validate).foreach(add)
    case Transactions(transactions: Seq[Transaction]) => transactions.filter(validate).foreach(add)
    case GetLastBlock => sender() ! blockChain.blocks.last
    case GetLastInfo => sender() ! LastInfo(blockChain.blocks, messagesHolder)
    case BlockchainRequest => sender() ! BlockchainAnswer(blockChain)
    case HeadersRequest => sender() ! HeadersAnswer(blockChain)
    case UserMessageFromCLI(message, outputId) =>
      self ! InfoMessage(
        UserMessage(message.mkString,
          toByteString(System.currentTimeMillis()),
          ByteString(keys.keys.head.publicKeyBytes),
          outputId.map(ByteString(_)),
          messagesHolder.size + 1)
      )
  }

  def add(modifier: Modifier): Unit = modifier match {
    case header: Header =>
      logger.info(s"Get header: ${header.asJson}")
      blockChain = blockChain.addHeader(header)
      if (settings.levelDB.enable)
        context.actorSelection("/user/starter/modifiersHolder") ! RequestModifiers(header)
    case payload: Payload =>
      logger.info(s"Get payload: ${payload.asJson}")
      blockChain = blockChain.addPayload(payload)
      if (settings.levelDB.enable)
        blockChain.sendBlock
      state = state.updateState(payload)
      if (settings.levelDB.enable)
        context.actorSelection("/user/starter/modifiersHolder") ! RequestModifiers(payload)
    case transaction: Transaction =>
      logger.info(s"Get transaction: ${transaction.asJson}")
      val payload: Payload = Payload(Seq(transaction))
      val headerUnsigned: Header = Header(
        System.currentTimeMillis(),
        blockChain.blockchainHeight + 1,
        blockChain.lastBlock.map(_.id).getOrElse(ByteString.empty),
        ByteString.empty,
        payload.id
      )
      val signedHeader: Header =
        headerUnsigned
          .copy(minerSignature = ByteString(Curve25519.sign(keys.keys.head.privKeyBytes, headerUnsigned.messageToSign.toArray)))
      add(signedHeader)
      add(payload)
      if (settings.levelDB.enable)
        context.actorSelection("/user/starter/modifiersHolder") ! RequestModifiers(transaction)
  }

  def addMessageAndCreateTx(msg: UserMessage): Option[Transaction] =
    if (!messagesHolder.contains(msg)) {
      if (messagesHolder.size % settings.mvpSettings.messagesQtyInChain == 0) {
      // Реинициализация
      currentSalt = randomByteString
      logger.info(s"Init new txChain with new salt: ${base16Encode(currentSalt)}")
      }
    val previousOutput: Option[OutputMessage] =
      state.state.values.toSeq.find {
        case output: OutputMessage =>
          output.messageHash ++ output.metadata ++ output.publicKey ==
            Sha256RipeMD160(ByteString(messagesHolder.last.message)) ++
              messagesHolder.last.metadata ++
              messagesHolder.last.sender
        case _ => false
      }.map(_.asInstanceOf[OutputMessage])
    Some(createMessageTx(msg, previousOutput))
  } else None

  def createMessageTx(message: UserMessage,
                      previousOutput: Option[OutputMessage]): Transaction = {
    logger.info(s"Get message: ${message.asJson}")
    messagesHolder = messagesHolder :+ message
    Generator.generateMessageTx(keys.keys.head,
      previousOutput.map(_.toProofGenerator),
      previousOutput.map(_.id),
      message,
      previousOutput.map(output =>
        if (output.txNum == 1) settings.mvpSettings.messagesQtyInChain + 1 else output.txNum)
        .getOrElse(settings.mvpSettings.messagesQtyInChain + 1),
      currentSalt
    )
  }

  def validate(modifier: Modifier): Boolean = modifier match {
    //TODO: Add semantic validation check
    case header: Header =>
      !blockChain.headers.map(header => base16Encode(header.id)).contains(base16Encode(header.id)) &&
      (header.height == 0 ||
        (header.height > blockChain.headers.last.height && blockChain.getHeaderAtHeight(header.height - 1)
        .exists(prevHeader => header.previousBlockHash == prevHeader.id)))
    case payload: Payload =>
        !blockChain.blocks.map(block => base16Encode(block.payload.id)).contains(base16Encode(payload.id)) &&
          payload.transactions.forall(validate)
    case transaction: Transaction =>
      logger.info(s"Going to validate tx: ${transaction.asJson}")
      transaction
        .inputs
        .forall(input => state.state.get(base16Encode(input.useOutputId))
          .exists(outputToUnlock => outputToUnlock.unlock(input.proofs) &&
            outputToUnlock.canBeSpent && outputToUnlock.checkSignature))
  }
}

case class LastInfo(blocks: Seq[Block], messages: Seq[UserMessage])

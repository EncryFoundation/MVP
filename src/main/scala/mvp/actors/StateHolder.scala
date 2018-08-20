package mvp.actors

import akka.actor.Actor
import com.google.common.primitives.Longs
import mvp.cli.ConsoleActor.{BlockchainRequest, HeadersRequest, UserMessageFromCLI}
import com.typesafe.scalalogging.StrictLogging
import mvp.data.{Blockchain, Modifier, State, _}
import io.circe.{Decoder, Encoder, HCursor}
import io.circe.syntax._
import mvp.MVP.settings
import mvp.actors.Messages._
import mvp.actors.ModifiersHolder.RequestModifiers
import mvp.local.messageHolder.UserMessage
import mvp.local.{Generator, Keys}
import mvp.utils.Crypto.Sha256RipeMD160
import scorex.crypto.signatures.Curve25519
import scorex.util.encode.Base16
import scorex.utils.Random

class StateHolder extends Actor with StrictLogging {
  var blockChain: Blockchain = Blockchain.recoverBlockchain
  var state: State = State.recoverState
  val keys: Keys = Keys.recoverKeys
  var messagesHolder: Seq[UserMessage] = Seq.empty
  var currentSalt: Array[Byte] = Random.randomBytes()

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
          Longs.toByteArray(System.currentTimeMillis()),
          keys.keys.head.publicKeyBytes,
          outputId,
          messagesHolder.size + 1)
      )
  }

  def add(modifier: Modifier): Unit = modifier match {
    case header: Header =>
      logger.info(s"Get header: ${Header.jsonEncoder(header)}")
      blockChain = blockChain.addHeader(header)
      if (settings.levelDB.enable)
        context.actorSelection("/user/starter/modifiersHolder") ! RequestModifiers(header)
    case payload: Payload =>
      logger.info(s"Get payload: ${Payload.jsonEncoder(payload)}")
      blockChain = blockChain.addPayload(payload)
      if (settings.levelDB.enable)
        blockChain.SendBlock
      state = state.updateState(payload)
      if (settings.levelDB.enable)
        context.actorSelection("/user/starter/modifiersHolder") ! RequestModifiers(payload)
    case transaction: Transaction =>
      logger.info(s"Get transaction: ${Transaction.jsonEncoder(transaction)}")
      val payload: Payload = Payload(Seq(transaction))
      val headerUnsigned: Header = Header(
        System.currentTimeMillis(),
        blockChain.blockchainHeight + 1,
        blockChain.lastBlock.map(_.id).getOrElse(Array.emptyByteArray),
        Array.emptyByteArray,
        payload.id
      )
      val signedHeader: Header =
        headerUnsigned
          .copy(minerSignature = Curve25519.sign(keys.keys.head.privKeyBytes, headerUnsigned.messageToSign))
      add(signedHeader)
      add(payload)
      if (settings.levelDB.enable)
        context.actorSelection("/user/starter/modifiersHolder") ! RequestModifiers(transaction)
  }

  def addMessageAndCreateTx(msg: UserMessage): Option[Transaction] =
    if (!messagesHolder.contains(msg)) {
      if (messagesHolder.size % settings.mvpSettings.messagesQtyInChain == 0) {
      // Реинициализация
      currentSalt = Random.randomBytes()
      logger.info(s"Init new txChain with new salt: ${Base16.encode(currentSalt)}")
      }
    val previousOutput: Option[OutputMessage] =
      state.state.values.toSeq.find {
        case output: OutputMessage =>
          output.messageHash ++ output.metadata ++ output.publicKey sameElements
            Sha256RipeMD160(messagesHolder.last.message.getBytes) ++
              messagesHolder.last.metadata ++
              messagesHolder.last.sender
        case _ => false
      }.map(_.asInstanceOf[OutputMessage])
    Some(createMessageTx(msg, previousOutput))
  } else None

  def createMessageTx(message: UserMessage,
                      previousOutput: Option[OutputMessage]): Transaction = {
    logger.info(s"Get message: ${UserMessage.jsonEncoder(message)}")
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
      !blockChain.headers.map(header => Base16.encode(header.id)).contains(Base16.encode(header.id)) &&
      (header.height == 0 ||
        (header.height > blockChain.headers.last.height && blockChain.getHeaderAtHeight(header.height - 1)
        .exists(prevHeader => header.previousBlockHash sameElements prevHeader.id)))
    case payload: Payload =>
        !blockChain.blocks.map(block => Base16.encode(block.payload.id)).contains(Base16.encode(payload.id)) &&
          payload.transactions.forall(validate)
    case transaction: Transaction =>
      logger.info(s"Going to validate tx: ${Transaction.jsonEncoder(transaction)}")
      transaction
        .inputs
        .forall(input => state.state.get(Base16.encode(input.useOutputId))
          .exists(outputToUnlock => outputToUnlock.unlock(input.proofs) &&
            outputToUnlock.canBeSpent && outputToUnlock.checkSignature))
  }
}

case class LastInfo(blocks: Seq[Block], messages: Seq[UserMessage])

object LastInfo {

  implicit val jsonDecoder: Decoder[LastInfo] = (c: HCursor) => for {
    blocks <- c.downField( "blocks" ).as[Seq[Block]]
    messages <- c.downField( "messages" ).as[Seq[UserMessage]]
  } yield LastInfo(
    blocks,
    messages
  )

  implicit val jsonEncoder: Encoder[LastInfo] = (b: LastInfo) => Map(
    "blocks" -> b.blocks.map( _.asJson ).asJson,
    "messages" -> b.messages.map( _.asJson ).asJson
  ).asJson
}

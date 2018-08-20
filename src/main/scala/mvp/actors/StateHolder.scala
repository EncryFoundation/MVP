package mvp.actors

import akka.actor.Actor
import com.google.common.primitives.Longs
import mvp.cli.ConsoleActor.{BlockchainRequest, HeadersRequest, SendMyName, UserMessageFromCLI}
import com.typesafe.scalalogging.StrictLogging
import mvp.data.{Blockchain, Modifier, State, _}
import io.circe.{Decoder, Encoder, HCursor}
import io.circe.syntax._
import mvp.MVP.settings
import mvp.actors.Messages._
import mvp.local.messageHolder.UserMessage
import mvp.local.messageTransaction.MessageInfo
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

  def apply(modifier: Modifier): Unit = modifier match {
    case header: Header =>
      logger.info(s"Get header: ${Header.jsonEncoder(header)}")
      blockChain = blockChain.addHeader(header)
    case payload: Payload =>
      logger.info(s"Get payload: ${Payload.jsonEncoder(payload)}")
      blockChain = blockChain.addPayload(payload)
      state = state.updateState(payload)
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
      apply(signedHeader)
      apply(payload)
  }

  def addMessage(message: UserMessage,
                 previousOutput: Option[OutputMessage]): Unit =
    if (!messagesHolder.contains(message)) {
      logger.info(s"Get message: ${UserMessage.jsonEncoder(message)}")
      messagesHolder = messagesHolder :+ message
      self ! Transactions(
        Seq(
          Generator.generateMessageTx(keys.keys.head,
            previousOutput.map(_.toProofGenerator),
            previousOutput.map(_.id),
            message,
            previousOutput.map(output => if (output.txNum == 1) 4 else output.txNum).getOrElse(4),
            currentSalt
          )
        )
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

  override def receive: Receive = {
    case Headers(headers: Seq[Header]) => headers.filter(validate).foreach(apply)
    case InfoMessage(msg: UserMessage) =>
      if (!messagesHolder.contains(msg)) {
        if (messagesHolder.size % 3 == 0) {
          // Реинициализация
          currentSalt = Random.randomBytes()
          logger.info(s"Init new txChain with new salt: ${Base16.encode(currentSalt)}")
        }
        state.state.values.toSeq.foreach {
          case output: OutputMessage if output.messageHash ++ output.metadata ++ output.publicKey sameElements
            Sha256RipeMD160(messagesHolder.last.message.getBytes) ++
              messagesHolder.last.metadata ++
              messagesHolder.last.sender => addMessage(msg, Some(output))
          case _ =>
        }
      }
    case Payloads(payloads: Seq[Payload]) => payloads.filter(validate).foreach(apply)
    case Transactions(transactions: Seq[Transaction]) => transactions.filter(validate).foreach(apply)
    case GetLastBlock => sender() ! blockChain.blocks.last
    case GetLastInfo => sender() ! LastInfo(blockChain.blocks, messagesHolder)
    case BlockchainRequest => sender() ! BlockchainAnswer(blockChain)
    case HeadersRequest => sender() ! HeadersAnswer(blockChain)
    case SendMyName =>
      self ! InfoMessage(
        UserMessage(settings.mvpSettings.nodeName,
          Longs.toByteArray(System.currentTimeMillis()),
          keys.keys.head.publicKeyBytes,
          None,
          messagesHolder.size + 1)
      )
    case UserMessageFromCLI(message, outputId) =>
      self ! InfoMessage(
        UserMessage(message.mkString,
          Longs.toByteArray(System.currentTimeMillis()),
          keys.keys.head.publicKeyBytes,
          outputId,
          messagesHolder.size + 1)
      )
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

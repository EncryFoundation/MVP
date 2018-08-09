package mvp.actors

import akka.actor.Actor
import io.circe.{Decoder, Encoder, HCursor}
import io.circe.syntax._
import mvp.actors.StateHolder._
import mvp.local.messageHolder.UserMessage
import mvp.local.messageTransaction.MessageInfo
import mvp.local.{Generator, Keys}
import mvp.modifiers.Modifier
import mvp.modifiers.blockchain.{Block, Header, Payload}
import mvp.modifiers.mempool.Transaction
import mvp.modifiers.state.output.MessageOutput
import mvp.view.blockchain.Blockchain
import mvp.view.state.State
import scorex.crypto.signatures.Curve25519

class StateHolder extends Actor {

  var blockChain: Blockchain = Blockchain.recoverBlockchain
  var state: State = State.recoverState
  val keys: Keys = Keys.recoverKeys
  var messagesHolder: Seq[UserMessage] = Seq.empty

  def apply(modifier: Modifier): Unit = modifier match {
    case header: Header =>
      blockChain = blockChain.addHeader(header)
    case payload: Payload =>
      blockChain = blockChain.addPayload(payload)
      state = state.updateState(payload)
    case transaction: Transaction =>
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
      self ! signedHeader
      self ! Payload
  }

  def addMessage(message: UserMessage, previousMessage: Option[MessageInfo], outputId: Option[Array[Byte]]): Unit = {
    messagesHolder = messagesHolder :+ message
    apply(Generator.generateMessageTx(keys.keys.head, previousMessage, outputId, message.message))
  }

  def validate(modifier: Modifier): Boolean = modifier match {
    //TODO: Add semantic validation check
    case header: Header =>
       header.height == 0 || (header.height > blockChain.headers.last.height && blockChain.getHeaderAtHeight(header.height - 1)
         .exists(prevHeader => header.previousBlockHash sameElements prevHeader.id))
    case payload: Payload =>
      payload.transactions.forall(validate)
    case transaction: Transaction =>
      transaction
        .inputs
        .forall(input => state.state.get(input.useOutputId)
          .exists(outputToUnlock => outputToUnlock.unlock(input.proof)))
  }

  override def receive: Receive = {
    case Headers(headers: Seq[Header]) => headers.filter(validate).foreach(apply)
    case Message(msg: UserMessage, previousOutputId: Option[Array[Byte]]) =>
      val previousMessageInfo: Option[MessageInfo] =
        previousOutputId.flatMap( outputId => state
          .state
          .get(outputId)
          .map(_.asInstanceOf[MessageOutput].toMessageInfo(msg.message)))
      addMessage(msg, previousMessageInfo, previousOutputId)
    case Payloads(payloads: Seq[Payload]) => payloads.filter(validate).foreach(apply)
    case Transactions(transactions: Seq[Transaction]) => transactions.filter(validate).foreach(apply)
    case GetLastBlock => sender() ! blockChain.blocks.last
    case GetLastInfo => sender() ! LastInfo(blockChain.blocks, messagesHolder)
  }
}

object StateHolder {

  case object GetLastInfo

  case class Message(msg: UserMessage, previousOutputId: Option[Array[Byte]])

  case object GetLastBlock

  case class Headers(headers: Seq[Header])

  case class Payloads(payloads: Seq[Payload])

  case class Transactions(transaction: Seq[Transaction])

  case class LastInfo(blocks: Seq[Block], messages: Seq[UserMessage])

  object LastInfo {

    implicit val jsonDecoder: Decoder[LastInfo] = (c: HCursor) => for {
      blocks <- c.downField("blocks").as[Seq[Block]]
      messages <- c.downField("proof").as[Seq[UserMessage]]
    } yield LastInfo(
      blocks,
      messages
    )

    implicit val jsonEncoder: Encoder[LastInfo] = (b: LastInfo) => Map(
      "blocks" -> b.blocks.map(_.asJson).asJson,
      "messages" -> b.messages.map(_.asJson).asJson
    ).asJson
  }
}
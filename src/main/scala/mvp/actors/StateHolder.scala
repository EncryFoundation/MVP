package mvp.actors

import akka.actor.Actor
import mvp.cli.ConsoleActor.{BlockchainRequest, HeadersRequest, SendMyName, UserMessageFromCLI}
import com.typesafe.scalalogging.StrictLogging
import mvp.data.{Blockchain, Modifier, State, _}
import io.circe.syntax._
import io.circe.generic.auto._
import mvp.MVP.settings
import mvp.actors.Messages._
import mvp.local.messageHolder.UserMessage._
import mvp.local.messageHolder.UserMessage
import mvp.local.messageTransaction.MessageInfo
import mvp.local.{Generator, Keys}
import scorex.crypto.signatures.Curve25519
import scorex.util.encode.Base16

class StateHolder extends Actor with StrictLogging {

  var blockChain: Blockchain = Blockchain.recoverBlockchain
  var state: State = State.recoverState
  val keys: Keys = Keys.recoverKeys
  var messagesHolder: Seq[UserMessage] = Seq.empty

  def apply(modifier: Modifier): Unit = modifier match {
    case header: Header =>
      logger.info(s"Get header: ${header.asJson}")
      blockChain = blockChain.addHeader(header)
    case payload: Payload =>
      logger.info(s"Get payload: ${payload.asJson}")
      blockChain = blockChain.addPayload(payload)
      state = state.updateState(payload)
    case transaction: Transaction =>
      logger.info(s"Get transaction: ${transaction.asJson}")
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

  def addMessage(message: UserMessage, previousMessage: Option[MessageInfo], outputId: Option[Array[Byte]]): Unit =
    if (!messagesHolder.contains(message)) {
      logger.info(s"Get message: ${message.asJson}")
      messagesHolder = messagesHolder :+ message
      self ! Transactions(Seq(Generator.generateMessageTx(keys.keys.head, previousMessage, outputId, message.message)))
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
      logger.info(s"Going to validate tx: ${transaction.asJson}")
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
        val previousMessageInfo: Option[MessageInfo] =
          msg.prevOutputId.flatMap( outputId =>
            state
              .state
              .get(Base16.encode(outputId))
              .map( _.asInstanceOf[OutputMessage].toProofGenerator )
          )
        addMessage( msg, previousMessageInfo, msg.prevOutputId )
      }
    case Payloads(payloads: Seq[Payload]) => payloads.filter(validate).foreach(apply)
    case Transactions(transactions: Seq[Transaction]) => transactions.filter(validate).foreach(apply)
    case GetLastBlock => sender() ! blockChain.blocks.last
    case GetLastInfo => sender() ! LastInfo(blockChain.blocks, messagesHolder)
    case BlockchainRequest => sender() ! BlockchainAnswer(blockChain)
    case HeadersRequest => sender() ! HeadersAnswer(blockChain)
    case SendMyName =>
      self ! InfoMessage(UserMessage(settings.mvpSettings.nodeName, keys.keys.head.publicKeyBytes, None))
    case UserMessageFromCLI(message, outputId) =>
      self ! InfoMessage(UserMessage(message.mkString, keys.keys.head.publicKeyBytes, outputId))
  }
}

case class LastInfo(blocks: Seq[Block], messages: Seq[UserMessage])

package mvp.actors

import java.security.KeyPair
import akka.actor.Actor
import akka.util.ByteString
import com.typesafe.scalalogging.StrictLogging
import io.circe.generic.auto._
import io.circe.syntax._
import mvp.actors.Messages._
import mvp.actors.ModifiersHolder.RequestModifiers
import mvp.cli.ConsoleActor._
import mvp.crypto.ECDSA
import mvp.crypto.Sha256.Sha256RipeMD160
import mvp.data.{Blockchain, Modifier, State, _}
import mvp.local.messageHolder.UserMessage
import mvp.local.{Generator, Keys}
import mvp.utils.Base16._
import mvp.utils.BlockchainUtils.{randomByteString, toByteString}
import mvp.utils.ECDSAUtils._
import mvp.utils.Settings.settings
import scala.util.Random
import mvp.utils.EncodingUtils._

class StateHolder extends Actor with StrictLogging {
  var blockChain: Blockchain = Blockchain.recoverBlockchain
  var state: State = State.recoverState
  val keys: Seq[KeyPair] = Keys.recoverKeys
  var messagesHolder: Seq[UserMessage] = Seq.empty
  var currentSalt: ByteString = randomByteString
  var wallet: Wallet = Wallet.recoverWallet(keys)

  override def receive: Receive = {
    case Headers(headers: Seq[Header]) => headers.filter(validateModifier).foreach(addModifier)
    case InfoMessage(msg: UserMessage) => addMessageAndCreateTx(msg).foreach(tx => self ! Transactions(Seq(tx)))
    case Payloads(payloads: Seq[Payload]) => payloads.filter(validateModifier).foreach(addModifier)
    case Transactions(transactions: Seq[Transaction]) => transactions.filter(validateModifier).foreach(addModifier)
    case GetLastBlock => sender() ! blockChain.blocks.last
    case GetLastInfo => sender() ! LastInfo(blockChain.blocks, messagesHolder)
    case BlockchainRequest => sender() ! BlockchainAnswer(blockChain)
    case HeadersRequest => sender() ! HeadersAnswer(blockChain)
    case UserMessageFromCLI(message) =>
      self ! InfoMessage(
        UserMessage(message.dropRight(1).mkString,
          toByteString(System.currentTimeMillis()),
          keys.head.getPublic,
          message.last.toLong,
          messagesHolder.size + 1)
      )
    case UserTransfer(recipient, amount, fee) =>
      self ! Transactions(Seq(createPaymentTx(recipient, amount, fee)))
    case MyAddress => sender() ! publicKey2Addr(keys.head.getPublic)
    case MyBalance => sender() ! wallet.balance
  }

  def addModifier(modifier: Modifier): Unit = modifier match {
    case header: Header =>
      logger.info(s"Get header: ${header.asJson}")
      blockChain = blockChain.addHeader(header)
      if (settings.levelDB.enable)
        context.actorSelection("/user/starter/modifiersHolder") ! RequestModifiers(header)
    case payload: Payload =>
      logger.info(s"Get payload: ${payload.asJson}")
      blockChain = blockChain.addPayload(payload)
      if (settings.levelDB.enable) blockChain.sendBlock
      state = state.updateState(payload)
      wallet = wallet.updateWallet(payload)
      if (settings.levelDB.enable)
        context.actorSelection("/user/starter/modifiersHolder") ! RequestModifiers(payload)
    case transaction: Transaction =>
      logger.info(s"Get transaction: ${transaction.asJson}")
      val payload: Payload = Payload(Seq(transaction) :+ createFeeTx(transaction.fee))
      val headerUnsigned: Header = Header(
        System.currentTimeMillis(),
        blockChain.blockchainHeight + 1,
        blockChain.lastBlock.map(_.id).getOrElse(ByteString.empty),
        ByteString.empty,
        payload.id
      )
      val signedHeader: Header =
        headerUnsigned
          .copy(minerSignature = ECDSA.sign(keys.head.getPrivate, headerUnsigned.messageToSign))
      addModifier(signedHeader)
      addModifier(payload)
      if (settings.levelDB.enable)
        context.actorSelection("/user/starter/modifiersHolder") ! RequestModifiers(transaction)
  }

  def validateModifier(modifier: Modifier): Boolean = modifier match {
    //TODO: Add semantic validation check + Fee-tx validation
    case header: Header =>
      !blockChain.headers.map(header => encode(header.id)).contains(encode(header.id)) &&
        (header.height == 0 ||
          (header.height > blockChain.headers.last.height && blockChain.getHeaderAtHeight(header.height - 1)
            .exists(prevHeader => header.previousBlockHash == prevHeader.id)))
    case payload: Payload =>
      //TODO: Fee tx valid
      !blockChain.blocks.map(block => encode(block.payload.id)).contains(encode(payload.id)) &&
        payload.transactions.dropRight(1).forall(validateModifier)
    case transaction: Transaction =>
      logger.info(s"Going to validate tx: ${transaction.asJson}")
      transaction
        .inputs
        .forall(input => state.state.get(encode(input.useOutputId))
          .exists(outputToUnlock => {
            logger.info(s"Result: ${outputToUnlock.unlock(input.proofs :+ transaction.messageToSign) &&
              outputToUnlock.canBeSpent}")
            outputToUnlock.unlock(input.proofs :+ transaction.messageToSign) &&
              outputToUnlock.canBeSpent
          }))
  }

  def addMessageAndCreateTx(msg: UserMessage): Option[Transaction] =
    if (!messagesHolder.contains(msg)) {
      if (messagesHolder.size % settings.mvpSettings.messagesQtyInChain == 0) {
      // Реинициализация
      currentSalt = Sha256RipeMD160(currentSalt)
      logger.info(s"Init new txChain with new salt: ${encode(currentSalt)}")
      }
    val previousOutput: Option[OutputMessage] =
      state.state.values.toSeq.find {
        case output: OutputMessage if messagesHolder.nonEmpty =>
          output.messageHash ++ output.metadata ++ ByteString(output.publicKey.getEncoded) ==
            Sha256RipeMD160(ByteString(messagesHolder.last.message)) ++
              messagesHolder.last.metadata ++
              ByteString(messagesHolder.last.sender.getEncoded)
        case _ => false
      }.map(_.asInstanceOf[OutputMessage])
    val boxesToFee: Seq[MonetaryOutput] = wallet
      .unspentOutputs.foldLeft(Seq[MonetaryOutput]()) {
      case (boxesToSpent, unspentBox) => unspentBox match {
        case monetaryOutput: MonetaryOutput =>
          if (boxesToSpent.map(_.amount).sum < msg.fee) boxesToSpent :+ monetaryOutput
          else boxesToSpent
        case _ => boxesToSpent
      }
    }
    Some(createMessageTx(msg, previousOutput, msg.fee, boxesToFee))
  } else None

  def createMessageTx(message: UserMessage,
                      previousOutput: Option[OutputMessage],
                      fee: Long,
                      boxesToFee: Seq[MonetaryOutput]): Transaction = {
    logger.info(s"Get message: ${message.asJson}")
    messagesHolder = messagesHolder :+ message
    Generator.generateMessageTx(keys.head.getPrivate,
      previousOutput.map(_.toProofGenerator),
      previousOutput.map(_.id),
      message,
      previousOutput.map(output =>
        if (output.txNum == 1) settings.mvpSettings.messagesQtyInChain + 1 else output.txNum)
        .getOrElse(settings.mvpSettings.messagesQtyInChain + 1),
      currentSalt,
      fee,
      boxesToFee,
      keys.head.getPublic
    )
  }

  def createPaymentTx(recipientAddr: ByteString,
                      amount: Long,
                      fee: Long): Transaction = {
    logger.info(s"Generating payment tx." +
      s" Recipient: $recipientAddr." +
      s" Amount: $amount." +
      s" Fee: $fee")
    val boxesToSpentInTx: Seq[MonetaryOutput] = wallet
      .unspentOutputs
      .foldLeft(Seq[MonetaryOutput]()) {
        case (boxesToSpent, unspentBox) => unspentBox match {
          case monetaryOutput: MonetaryOutput =>
            if (boxesToSpent.map (_.amount).sum < amount + fee) boxesToSpent :+ monetaryOutput
            else boxesToSpent
          case _ => boxesToSpent
        }
      }
    val charge: Long = boxesToSpentInTx.map(_.amount).sum - (amount + fee)
    val inputs: Seq[Input] = boxesToSpentInTx.map(box => Input(box.id, Seq.empty))
    val outputs: Seq[OutputAmount] = {
      //Nonce should't be random, only generation from tx id
      val boxToRecipient: OutputAmount = OutputAmount(recipientAddr, amount, Random.nextLong())
      if (charge > 0) Seq(boxToRecipient, OutputAmount(publicKey2Addr(keys.head.getPublic), charge, Random.nextLong()))
      else Seq(boxToRecipient)
    }
    val unsignedTx = Transaction(System.currentTimeMillis(), fee, inputs, outputs)
    val sing: ByteString = ECDSA.sign(keys.head.getPrivate, unsignedTx.messageToSign)
    val signedPaymentInputs: Seq[Input] =
      boxesToSpentInTx.map(box => Input(box.id, Seq(sing, ByteString(keys.head.getPublic.getEncoded))))
    unsignedTx.copy(inputs = signedPaymentInputs)
  }

  def createFeeTx(fee: Long): Transaction = {
    Transaction(
      System.currentTimeMillis(),
      0L,
      Seq.empty,
      Seq(
        OutputAmount(publicKey2Addr(keys.head.getPublic), fee, Random.nextLong())
      )
    )
  }
}

case class LastInfo(blocks: Seq[Block], messages: Seq[UserMessage])
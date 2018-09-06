package mvp.actors

import akka.persistence.{PersistentActor, RecoveryCompleted}
import akka.util.ByteString
import com.typesafe.scalalogging.StrictLogging
import mvp.actors.ModifiersHolder.{MessagesFromLevelDB, RequestModifiers, RequestUserMessage, Statistics}
import mvp.data._
import mvp.local.messageHolder.UserMessage
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.immutable.SortedMap
import mvp.utils.Settings.settings
import mvp.actors.Messages.{Headers, Payloads}
import mvp.crypto.Sha256.Sha256RipeMD160
import mvp.utils.Base16._
import scala.concurrent.duration._

class ModifiersHolder extends PersistentActor with StrictLogging {

  var headers: Map[String, Header] = Map.empty
  var payloads: Map[String, Payload] = Map.empty
  var messages: SortedMap[String, UserMessage] = SortedMap.empty
  var blocks: SortedMap[Int, Block] = SortedMap.empty
  var transactions: Map[String, Transaction] = Map.empty

  context.system.scheduler.schedule(5.second, 60.second) {
    logger.debug(Statistics(headers, payloads, messages, transactions, blocks).toString)
  }

  // TODO cant send Seq[Header] to StateHolder actor
  override def preStart(): Unit = logger.info(s"ModifiersHolder actor is started.")

  override def receiveRecover: Receive =
    if (settings.levelDB.recoverMode) receiveRecoveryEnable else receiveRecoveryDisable

  def receiveRecoveryEnable: Receive = {
    case header: Header =>
      updateHeaders(header)
      logger.debug(s"Header ${header.height} with id ${encode(header.id)} is recovered from leveldb.")
    case payload: Payload =>
      updatePayloads(payload)
      logger.debug(s"Payload is recovered from leveldb.")
    case message: UserMessage =>
      updateMessages(message)
      logger.debug(s"Message from ${encode(ByteString(message.sender.getEncoded))} is recovered from leveldb")
    case transaction: Transaction =>
      updateTransactions(transaction)
      logger.debug(s"Transaction with id ${encode(transaction.id)} is recovered from leveldb")
    case block: Block =>
      updateBlock(block)
      logger.debug(s"Block with id ${encode(block.id)} is recovered from leveldb")
    case RecoveryCompleted =>
      headers.values.toSeq
        .sortWith((headerOne, headerTwo) => headerOne.height < headerTwo.height).foreach(header =>
        context.system.actorSelection("user/stateHolder") ! Headers(Seq(header)))
      logger.debug("Headers are successfully recovered.")
      payloads.foreach(payload =>
        context.system.actorSelection("user/stateHolder") ! Payloads(Seq(payload._2)))
      logger.debug("Payloads are successfully recovered.")
      messages.foreach(messages =>
        context.system.actorSelection("user/stateHolder") ! MessagesFromLevelDB(messages._2))
      logger.debug("Messages are successfully recovered.")
      if (blocks.nonEmpty) logger.debug("Blocks are successfully recovered!")
      if (transactions.nonEmpty) logger.debug("Transactions are successfully recovered!")
      logger.info("Recovery is completed.")
    case _ =>
  }

  def receiveRecoveryDisable: Receive = {
    case _ => logger.info("Recovery is disabled.")
  }

  override def receiveCommand: Receive = {
    case RequestModifiers(modifier: Modifier) => saveModifiers(modifier)
    case RequestUserMessage(message: UserMessage) => saveUserMessage(message)
    case x: Any => logger.error(s"Strange input: $x.")
  }

  def saveModifiers(modifiers: Modifier): Unit = modifiers match {
    case header: Header =>
      if (!headers.contains(encode(header.id)))
        persist(header) { header =>
          logger.debug(s"Header at height: ${header.height} with id: ${encode(header.id)} " +
            s"is persisted successfully.")
        }
      updateHeaders(header)
    case payload: Payload =>
      if (!payloads.contains(encode(payload.id)))
        persist(payload) { payload =>
          logger.debug(s"Payload with id: ${encode(payload.id)} is persisted successfully.")
        }
      updatePayloads(payload)
    case transaction: Transaction =>
      if (!transactions.contains(encode(transaction.id)))
        persist(transaction) { tx =>
          logger.debug(s"Transaction with id: ${encode(tx.id)} is persisted successfully.")
        }
      updateTransactions(transaction)
    case block: Block =>
      if (!blocks.values.toSeq.contains(block))
        persist(block) { block =>
          logger.debug(s"Block with id: ${encode(block.id)} is persisted successfully.")
        }
      updateBlock(block)
    case x: Any => logger.error(s"Strange input $x")
  }

  def saveUserMessage(message: UserMessage): Unit = {
    if (!messages.contains(message.message))
      persist(message) { message =>
        logger.debug(s"Message ${message.message} with prevId ${message.prevOutputId} is persisted successfully.")
      }
    updateMessages(message)
  }

  def updateHeaders(header: Header): Unit = headers += encode(header.id) -> header

  def updatePayloads(payload: Payload): Unit = payloads += encode(payload.id) -> payload

  def updateTransactions(transaction: Transaction): Unit = transactions += encode(transaction.id) -> transaction

  def updateMessages(message: UserMessage): Unit =
    messages +=
      Sha256RipeMD160(ByteString(message.message.getBytes) ++
        ByteString(message.sender.getEncoded) ++
        message.prevOutputId.getOrElse(ByteString.empty)).mkString -> message

  def updateBlock(block: Block): Unit = blocks += block.header.height -> block

  override def persistenceId: String = "persistent actor"

  override def journalPluginId: String = "akka.persistence.journal.leveldb"

  override def snapshotPluginId: String = "akka.persistence.snapshot-store.local"

}

object ModifiersHolder {

  case class RequestModifiers(modifier: Modifier)

  case class RequestUserMessage(messages: UserMessage)

  case class MessagesFromLevelDB(message: UserMessage)

  case class Statistics(receivedHeaders: Int,
                        receivedPayloads: Int,
                        receivedMessages: Int,
                        receivedTransactions: Int,
                        receivedBlocks: Int,
                        currentBlockChainHeight: Option[Int],
                        currentHeadersHeight: Option[Int]) {
    override def toString: String =
      s"Stats: ${currentBlockChainHeight.getOrElse(0)} blockChain height, " +
        s"${currentHeadersHeight.getOrElse(0)} headers height " +
        s"$receivedHeaders headers, " +
        s"$receivedPayloads payloads, " +
        s"$receivedMessages messages, " +
        s"$receivedTransactions transactions, " +
        s"$receivedBlocks blocks."
  }

  object Statistics {
    def apply(receivedHeaders: Map[String, Header],
              receivedPayloads: Map[String, Payload],
              receivedMessages: SortedMap[String, UserMessage],
              receivedTransactions: Map[String, Transaction],
              receivedBlocks: SortedMap[Int, Block]): Statistics =
      Statistics(
        receivedHeaders.size,
        receivedPayloads.size,
        receivedMessages.size,
        receivedTransactions.size,
        receivedBlocks.size,
        receivedBlocks.values.toSeq.sortBy(_.header.height).lastOption.map(_.header.height),
        receivedHeaders.values.toSeq.sortBy(_.height).lastOption.map(_.height)
      )
  }

}
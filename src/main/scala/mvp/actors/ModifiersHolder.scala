package mvp.actors

import akka.persistence.{PersistentActor, RecoveryCompleted}
import com.typesafe.scalalogging.StrictLogging
import mvp.actors.ModifiersHolder.{RequestBlock, RequestModifiers, RequestUserMessage}
import mvp.local.messageHolder.UserMessage
import mvp.modifiers.Modifier
import mvp.modifiers.blockchain.{Block, Header, Payload}
import mvp.modifiers.mempool.Transaction
import scorex.util.encode.Base16
import scala.collection.immutable.SortedMap
import mvp.MVP.settings

class ModifiersHolder extends PersistentActor with StrictLogging {

  var headers: Map[String, (Header, Int)] = Map.empty
  var payloads: Map[String, (Payload, Int)] = Map.empty
  var transactions: Map[String, (Transaction, Int)] = Map.empty
  var blocks: SortedMap[Int, Block] = SortedMap.empty
  var messages: SortedMap[String, UserMessage] = SortedMap.empty

  override def preStart(): Unit = logger.info(s"ModifiersHolder actor is started.")

  override def receiveRecover: Receive = if (settings.levelDB.recoverMode) receiveRecoveryEnable else receiveRecoveryDisable

  def receiveRecoveryEnable: Receive = {
    case header: Header =>
      updateHeaders(header)
      logger.debug(s"Header ${header.height} recovered from leveldb.")
    case payload: Payload =>
      updatePayloads(payload)
      logger.debug(s"Payload recovered from leveldb.")
    case block: Block =>
      updateBlocks(block)
      logger.debug(s"Block ${block.header.height} recovered from leveldb.")
    case transaction: Transaction =>
      updateTransactions(transaction)
      logger.debug(s"Transaction with id ${Base16.encode(transaction.id)} recovered from leveldb")
    case message: UserMessage =>
      updateMessages(message)
      logger.debug(s"Message from ${Base16.encode(message.sender)} recovered from leveldb")
    case RecoveryCompleted => logger.info("Recovery completed.")
  }

  def receiveRecoveryDisable: Receive = {
    case _ => logger.info("Recovery disabled")
  }

  override def receiveCommand: Receive = {
    case RequestModifiers(modifier: Modifier) => saveModifiers(modifier)
    case RequestUserMessage(message: UserMessage) => saveUserMessage(message)
    case RequestBlock(block: Block) => saveBlock(block)
    case x: Any => logger.error(s"Strange input: $x.")
  }

  def saveModifiers(modifiers: Modifier): Unit = modifiers match {
    case header: Header =>
      persist(header) { header =>
        logger.debug(s"Header at height: ${header.height} with id: ${Base16.encode(header.id)} persisted successfully.")
      }
      updateHeaders(header)
    case payload: Payload =>
      persist(payload) { payload =>
        logger.debug(s"Payload with id: ${Base16.encode(payload.id)} persisted successfully.")
      }
      updatePayloads(payload)
    case block: Block =>
      persist(block) { block =>
        logger.debug(s"Header at height: ${block.header.height} with id: ${Base16.encode(block.id)} persisted successfully.")
      }
      updateBlocks(block)
    case transaction: Transaction =>
      persist(transaction) { transaction =>
        logger.debug(s"Transaction ${Base16.encode(transaction.id)} persisted successfully.")
      }
      updateTransactions(transaction)
    case x: Any => logger.error(s"Strange input $x")
  }

  def saveBlock(block: Block): Unit = {
    persist(block) { block =>
      logger.debug(s"Block ${block.header} with id ${Base16.encode(block.id)} persisted successfully.")
    }
    updateBlocks(block)
  }

  def saveUserMessage(message: UserMessage): Unit = {
    persist(message) { message =>
      logger.debug(s"Message ${message.message} with prevId ${message.prevOutputId} persisted successfully.")
    }
    updateMessages(message)
  }

  def updateHeaders(header: Header): Unit = {
    val prevValue: (Header, Int) = headers.getOrElse(Base16.encode(header.id), (header, -1))
    headers += Base16.encode(header.id) -> (prevValue._1, prevValue._2 + 1)
  }

  def updatePayloads(payload: Payload): Unit = {
    val prevValue: (Payload, Int) = payloads.getOrElse(Base16.encode(payload.id), (payload, -1))
    payloads += Base16.encode(payload.id) -> (prevValue._1, prevValue._2 + 1)
  }

  def updateBlocks(block: Block): Unit = blocks += block.header.height -> block

  def updateMessages(message: UserMessage): Unit = messages += Base16.encode(message.sender) -> message

  def updateTransactions(transaction: Transaction): Unit = {
    val prevValue: (Transaction, Int) = transactions.getOrElse(Base16.encode(transaction.id), (transaction, -1))
    transactions += Base16.encode(transaction.id) -> (prevValue._1, prevValue._2)
  }

  override def persistenceId: String = "persistent actor"

  override def journalPluginId: String = "akka.persistence.journal.leveldb"

  override def snapshotPluginId: String = "akka.persistence.snapshot-store.local"

}

object ModifiersHolder {

  case class RequestModifiers(modifier: Modifier)

  case class RequestBlock(block: Block)

  case class RequestUserMessage(messages: UserMessage)

}
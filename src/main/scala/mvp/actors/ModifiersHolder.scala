package mvp.actors

import akka.persistence.{PersistentActor, RecoveryCompleted}
import com.typesafe.scalalogging.StrictLogging
import mvp.actors.ModifiersHolder.{RequestBlock, RequestModifiers, RequestUserMessage}
import mvp.local.messageHolder.UserMessage
import mvp.modifiers.Modifier
import mvp.modifiers.blockchain.{Block, Header, Payload}
import mvp.modifiers.mempool.Transaction
import scorex.util.encode.Base58

import scala.collection.immutable.SortedMap

class ModifiersHolder extends PersistentActor with StrictLogging {

  var headers: Map[String, (Header, Int)] = Map.empty
  var payloads: Map[String, (Payload, Int)] = Map.empty
  var blocks: SortedMap[Int, Block] = SortedMap.empty

  override def preStart(): Unit = logger.info(s"ModifiersHolder actor is started.")

  override def receiveRecover: Receive = {
    case header: Header =>
      updateHeaders(header)
      logger.debug(s"Header ${header.height} is recovered from leveldb.")
    case payload: Payload =>
      updatePayloads(payload)
      logger.debug(s"Payload is recovered from leveldb.")
    case block: Block =>
      updateBlocks(block)
      logger.debug(s"Block ${block.header.height} is recovered from leveldb.")
    case RecoveryCompleted => logger.info("Recovery completed.")
  }

  override def receiveCommand: Receive = {
    case RequestModifiers(modifier: Modifier) => updateModifiers(modifier)
    case RequestUserMessage(message: UserMessage) => updateUserMessage(message)
    case RequestBlock(block: Block) => updateBlock(block)
    case x: Any => logger.error(s"Strange input: $x.")
  }

  def updateModifiers(modifiers: Modifier): Unit = modifiers match {
    case header: Header =>
      persist(header) { header =>
        logger.debug(s"Header at height: ${header.height} with id: ${Base58.encode(header.id)} persisted successfully.")
      }
    case payload: Payload =>
      persist(payload) { payload =>
        logger.debug(s"Payload with id: ${Base58.encode(payload.id)} persisted successfully.")
      }
    case block: Block =>
      persist(block) { block =>
        logger.debug(s"Header at height: ${block.header.height} with id: ${Base58.encode(block.id)} persisted successfully.")
      }
    case transaction: Transaction =>
      persist(transaction) { transaction =>
        logger.debug(s"Transaction ${Base58.encode(transaction.id)} persisted successfully.")
      }
    case x: Any => logger.error(s"Strange input $x")
  }

  def updateBlock(block: Block) = {
    persist(block) { block =>
      logger.debug(s"Block ${block.header} with id ${Base58.encode(block.id)} persisted successfully.")
    }
  }

  def updateUserMessage(message: UserMessage): Unit = {
    persist(message) { message =>
      logger.debug(s"Message ${message.message} with prevId ${message.prevOutputId} persisted successfully.")
    }
  }

  def updateHeaders(header: Header): Unit = ???

  def updatePayloads(payload: Payload): Unit = ???

  def updateBlocks(pblock: Block): Unit = ???

  override def persistenceId: String = "persistent actor"

  override def journalPluginId: String = "akka.persistence.journal.leveldb"

  override def snapshotPluginId: String = "akka.persistence.snapshot-store.local"

}

object ModifiersHolder {

  case class RequestModifiers(modifier: Modifier)

  case class RequestBlock(block: Block)

  case class RequestUserMessage(messages: UserMessage)

}
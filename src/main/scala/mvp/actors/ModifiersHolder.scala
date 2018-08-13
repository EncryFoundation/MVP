package mvp.actors

import akka.persistence.{PersistentActor, RecoveryCompleted}
import com.typesafe.scalalogging.StrictLogging
import mvp.actors.ModifiersHolder.RequestModifiers
import mvp.modifiers.blockchain.{Block, Header, Payload}

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
    case RequestModifiers(modifier: Header) => updateModifiers(modifier)
    case x: Any => logger.error(s"Strange input: $x.")
  }

  def updateModifiers(modifier: Header) = {
    persist(modifier) { header =>
      logger.debug(s"Header at height: ${header.height} with id: ${} is persisted successfully.")
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

  case class RequestModifiers(modifier: Any)

}
package mvp.actors

import akka.persistence.{PersistentActor, RecoveryCompleted}
import com.typesafe.scalalogging.StrictLogging
import mvp.actors.ModifiersHolder.{ModifierFromLevelDB, RequestModifiers, RequestUserMessage, Statistics}
import mvp.data._
import mvp.local.messageHolder.UserMessage
import scorex.util.encode.Base16
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.immutable.SortedMap
import mvp.MVP.settings
import mvp.actors.Messages.{Headers, InfoMessage, Payloads}
import scala.concurrent.duration._

class ModifiersHolder extends PersistentActor with StrictLogging {

  var headers: Map[String, (Header, Int)] = Map.empty
  var payloads: Map[String, (Payload, Int)] = Map.empty
  var messages: SortedMap[String, UserMessage] = SortedMap.empty

  context.system.scheduler.schedule(5.second, 5.second) {
    logger.debug(Statistics(headers.size, payloads.size, messages.size).toString)
  }

  override def preStart(): Unit = logger.info(s"ModifiersHolder actor is started.")

  override def receiveRecover: Receive = if (settings.levelDB.recoverMode) receiveRecoveryEnable else receiveRecoveryDisable

  def receiveRecoveryEnable: Receive = {
    case header: Header =>
      updateHeaders(header)
      logger.debug(s"Header ${header.height} with id ${Base16.encode(header.id)} recovered from leveldb.")
    case payload: Payload =>
      updatePayloads(payload)
      logger.debug(s"Payload recovered from leveldb.")
    case message: UserMessage =>
      updateMessages(message)
      logger.debug(s"Message from ${Base16.encode(message.sender)} recovered from leveldb")
    case RecoveryCompleted =>
      val test: Seq[Header] = Seq.empty
      val headers1: Seq[Header] = headers.foldLeft(test) { case (a, b) => a :+ b._2._1 }
      headers1.foreach(a => println(Base16.encode(a.id) + "RecoveryComlete"))
      context.system.actorSelection("user/stateHolder") ! Headers(headers1)
      logger.debug("Headers succesfully recovered!")
      logger.debug("Transactions succesfully recovered!")
      payloads.foreach(payload =>
        context.system.actorSelection("user/stateHolder") ! Payloads(Seq(payload._2._1))
      )
      logger.debug("Payloads succesfully recovered!")
      messages.foreach(messages =>
        context.system.actorSelection("user/stateHolder") ! InfoMessage(messages._2)
      )
      logger.debug("Messages succesfully recovered!")
      logger.info("Recovery completed.")
    case _ =>
  }

  def receiveRecoveryDisable: Receive = {
    case _ => logger.info("Recovery disabled")
  }

  override def receiveCommand: Receive = {
    case RequestModifiers(modifier: Modifier) => saveModifiers(modifier)
    case RequestUserMessage(message: UserMessage) => saveUserMessage(message)
    case x: Any => logger.error(s"Strange input: $x.")
  }

  def saveModifiers(modifiers: Modifier): Unit = modifiers match {
    case header: Header =>
      if (!headers.contains(Base16.encode(header.id)))
        persist(header) { header =>
          logger.debug(s"Header at height: ${header.height} with id: ${Base16.encode(header.id)} persisted successfully.")
        }
      updateHeaders(header)
    case payload: Payload =>
      if (!payloads.contains(Base16.encode(payload.id))) {
        persist(payload) { payload =>
          logger.debug(s"Payload with id: ${Base16.encode(payload.id)} persisted successfully.")
        }
        updatePayloads(payload)
      }
    case x: Any => logger.error(s"Strange input $x")
  }

  def saveUserMessage(message: UserMessage): Unit = {
    if (messages.contains(Base16.encode(message.sender)))
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

  def updateMessages(message: UserMessage): Unit = messages += Base16.encode(message.sender) -> message

  override def persistenceId: String = "persistent actor"

  override def journalPluginId: String = "akka.persistence.journal.leveldb"

  override def snapshotPluginId: String = "akka.persistence.snapshot-store.local"

}

object ModifiersHolder {

  case class RequestModifiers(modifier: Modifier)

  case class RequestUserMessage(messages: UserMessage)

  case class ModifierFromLevelDB(modifier: Modifier)

  case class Statistics(receivedHeaders: Int,
                        receivedPayloads: Int,
                        receivedMessages: Int) {
    override def toString: String = s"Stats: $receivedHeaders headers, " +
      s"$receivedPayloads payloads, " +
      s"$receivedMessages messages, "
  }

}
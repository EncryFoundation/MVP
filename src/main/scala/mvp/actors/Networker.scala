package mvp.actors

import java.net.InetSocketAddress
import akka.actor.{Actor, ActorRef, Props}
import akka.util.ByteString
import com.typesafe.scalalogging.StrictLogging
import mvp.MVP.settings
import mvp.actors.Messages._
import mvp.utils.EncodingUtils._
import io.circe.syntax._
import io.circe.generic.auto._
import io.circe.parser.decode

class Networker extends Actor with StrictLogging {

  override def preStart(): Unit = super.preStart()

  override def receive: Receive = {
    case Start if settings.testMode =>
      logger.info("test mode on networker")
      bornKids()
    case data: ByteString if data == RequestLastInfo.bytes =>
      logger.info("Received last info request")
      context.actorSelection("/user/stateHolder") ! GetLastInfo
    case data: ByteString if decode[LastInfo](data.utf8String).isRight =>
      decode[LastInfo](data.utf8String).map {
        case LastInfo(blocks, messages) =>
          logger.info(s"Get blocks from remote: ${blocks.map(_.asJson).mkString("\n")}")
          logger.info(s"Get messages from remote: ${messages.map(_.asJson).mkString("\n")}")
          context.system.actorSelection("user/stateHolder") ! Headers(blocks.map(_.header))
          context.system.actorSelection("user/stateHolder") ! Payloads(blocks.map(_.payload))
      }
    case data: ByteString => logger.info(s"Got data from remote ${data.toString()}")
    case li: LastInfo =>
      logger.info("Sending last info")
      context.actorSelection("/user/starter/networker/receiver") ! ByteString(li.asJson.toString())
    case remote: InetSocketAddress => logger.info(remote.toString)
    case Heartbeat =>
      logger.info("heartbeat pong")
      context.actorSelection("/user/starter/networker/receiver") ! RequestLastInfo.bytes

  }

  override def postStop(): Unit = super.postStop()

  def bornKids(): Unit = {
    val receiver: ActorRef =
      context.actorOf(Props[UDPActor].withDispatcher("net-dispatcher").withMailbox("net-mailbox"), "receiver")
      receiver ! Start

  }
}

case object RequestLastInfo {
  val bytes: ByteString = ByteString.fromArray(Array.fill(32)(0))
}
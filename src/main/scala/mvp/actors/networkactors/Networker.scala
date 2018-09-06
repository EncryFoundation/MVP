package mvp.actors.networkactors

import akka.actor.{Actor, Props}
import com.typesafe.scalalogging.StrictLogging
import io.circe.generic.auto._
import io.circe.syntax._
import mvp.utils.EncodingUtils._
import mvp.utils.Settings.settings
import mvp.actors.Messages._
import mvp.actors.networkactors.NetworkMessages.RequestLastInfo
import mvp.actors.LastInfo

class Networker extends Actor with StrictLogging {

  override def preStart(): Unit = bornKids()

  override def receive: Receive = {
    case li: LastInfo =>
      logger.info("Sending last info")
      context.actorSelection("/user/starter/networker/sender") ! li
    case Heartbeat =>
      logger.info("heartbeat pong")
      context.actorSelection("/user/starter/networker/sender") ! RequestLastInfo
    case GetLastInfo => context.actorSelection("/user/stateHolder") ! GetLastInfo
    case LastInfoFromRemote(li) =>
      logger.info(s"Get blocks from remote: ${li.blocks.map(_.asJson).mkString("\n")}")
      logger.info(s"Get messages from remote: ${li.messages.map(_.asJson).mkString("\n")}")
      context.system.actorSelection("user/stateHolder") ! Headers(li.blocks.map(_.header))
      context.system.actorSelection("user/stateHolder") ! Payloads(li.blocks.map(_.payload))
  }

  override def postStop(): Unit = super.postStop()

  def bornKids(): Unit = if (settings.mvpSettings.useUDP) bornUdpKids() else bornHttpKids()

  def bornHttpKids(): Unit = {
    context.actorOf(Props[HttpActor].withDispatcher("net-dispatcher").withMailbox("net-mailbox"), "sender")
  }

  def bornUdpKids(): Unit = {
    context.actorOf(Props[UDPActor].withDispatcher("net-dispatcher").withMailbox("net-mailbox"), "udp")
    context.actorOf(Props[UdpReceiver].withDispatcher("net-dispatcher").withMailbox("net-mailbox"), "receiver")
    context.actorOf(Props[UdpSender].withDispatcher("net-dispatcher").withMailbox("net-mailbox"), "sender")
  }
}
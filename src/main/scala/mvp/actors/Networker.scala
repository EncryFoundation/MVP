package mvp.actors

import java.net.InetSocketAddress
import akka.actor.{Actor, ActorRef, Props}
import akka.util.ByteString
import com.typesafe.scalalogging.StrictLogging
import mvp.MVP.settings
import mvp.actors.Messages.Start

class Networker extends Actor with StrictLogging {

  override def preStart(): Unit = super.preStart()

  override def receive: Receive = {
    case Start if settings.testMode =>
      logger.info("test mode on networker")
    //startKids()
    case data: ByteString => logger.info(data.toString())
    case remote: InetSocketAddress => logger.info(remote.toString)
    case _ =>
  }

  override def postStop(): Unit = super.postStop()

  def startKids(): Unit = {
    val receiver: ActorRef =
      context.actorOf(Props[Receiver].withDispatcher("net-dispatcher").withMailbox("net-mailbox"), "receiver")
    //system.actorSelection("/user/starter/networker/receiver") ! Start
    receiver ! Start
    val sender: ActorRef =
      context.actorOf(Props[Sender].withDispatcher("net-dispatcher").withMailbox("net-mailbox"), "sender")
    //system.actorSelection("/user/starter/networker/sender") ! Start
    sender ! Start
  }
}
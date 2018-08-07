package mvp.actors

import java.net.InetSocketAddress
import akka.actor.{Actor, ActorRef, Props}
import akka.io.{IO, Udp}
import akka.util.ByteString
import com.typesafe.scalalogging.StrictLogging
import mvp.MVP.settings
import mvp.actors.Messages.Start

class Sender extends Actor with StrictLogging {

  import context.system

  IO(Udp) ! Udp.SimpleSender

  val remote: InetSocketAddress = new InetSocketAddress(settings.thisNode.host, settings.thisNode.port)
  logger.info(remote.toString)

  override def receive: Receive = {
    case Udp.SimpleSenderReady =>
      logger.info("Context on sender is switched")
      context.become(ready(sender()))
    case Start if settings.testMode => logger.info("test mode on sender")
    case _ =>
  }

  def ready(send: ActorRef): Receive = {
    case msg: String =>
      logger.info(send.path.toString)
      send ! Udp.Send(ByteString(msg), remote)
  }
}

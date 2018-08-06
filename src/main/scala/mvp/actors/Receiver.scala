package mvp.actors

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef}
import akka.io.{IO, Udp}
import akka.util.ByteString
import com.typesafe.scalalogging.StrictLogging
import mvp.MVP.settings
import mvp.actors.Messages.Start

class Receiver extends Actor with StrictLogging {

  import context.system

  IO(Udp) ! Udp.Bind(self, new InetSocketAddress(settings.thisNode.host, settings.thisNode.port))

  override def receive: Receive = {
    case Udp.Bound(local) =>
      logger.info("Context on receiver is switched")
      context.become(ready(sender()))
    case Start if settings.testMode => logger.info("test mode on receiver")
    case _ =>
  }

  def ready(socket: ActorRef): Receive = {
    case Udp.Received(data: ByteString, remote: InetSocketAddress) =>
      logger.info("received smth on receiver")

      context.parent ! data
      context.parent ! remote
    case Udp.Unbind => socket ! Udp.Unbind
    case Udp.Unbound => context.stop(self)
    case any: Any => logger.info(any.toString)
  }
}


package mvp.actors

import java.net.InetSocketAddress
import akka.actor.{Actor, ActorRef}
import akka.io.{IO, Udp}
import akka.util.ByteString
import com.typesafe.scalalogging.StrictLogging
import mvp.MVP.settings
import mvp.actors.Messages.Start

class UDPActor extends Actor with StrictLogging {

  import context.system

  val remote: InetSocketAddress = new InetSocketAddress("localhost", settings.otherNodes.head.port)

  override def receive: Receive = {
    case Udp.Bound(local) =>
      logger.info(s"Binded to $local")
      context.become(ready(sender()))
    case Start if settings.testMode =>
      logger.info("test mode on receiver")
      IO(Udp) ! Udp.Bind(self, new InetSocketAddress("localhost", settings.thisNode.port))
    case _ =>
  }

  def ready(socket: ActorRef): Receive = {
    case msg: ByteString =>
      logger.info(s"Sending ${msg.toString()} to $remote (this is ${settings.thisNode.port})")
      socket ! Udp.Send(msg, remote)
    case Udp.Received(data: ByteString, remote: InetSocketAddress) =>
      logger.info(s"Received ${data.toString} from $remote")
      context.parent ! data
      context.parent ! remote
    case Udp.Unbind =>
      logger.info("Unbind")
      socket ! Udp.Unbind
    case Udp.Unbound =>
      logger.info("Unbound")
      context.stop(self)
    case any: Any => logger.info(any.toString)
  }
}


package mvp.actors

import java.net.InetSocketAddress
import akka.actor.{Actor, ActorRef}
import akka.io.Udp
import akka.util.ByteString
import mvp.MVP.settings
import mvp.actors.Messages.Start

class Receiver extends Actor {
  override def receive: Receive = {
    case Udp.Bound(local) ⇒
      context.become(ready(sender()))
    case Start if settings.testMode =>
      println("test mode on networker")
    case _ =>
  }

  def ready(socket: ActorRef): Receive = {
    case Udp.Received(data: ByteString, remote: InetSocketAddress) ⇒
      context.parent ! remote
      context.parent ! remote
    case Udp.Unbind ⇒ socket ! Udp.Unbind
    case Udp.Unbound ⇒ context.stop(self)
  }
}


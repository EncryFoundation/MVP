package mvp.actors

import java.net.InetSocketAddress
import akka.actor.{Actor, ActorRef}
import akka.io.{IO, Udp}
import akka.util.ByteString
import mvp.MVP.settings
import mvp.actors.Messages.Start

class Sender(receiver: InetSocketAddress) extends Actor {

  import context.system

  IO(Udp) ! Udp.SimpleSender

  override def receive: Receive = {
    case Udp.SimpleSenderReady ⇒ context.become(ready(sender()))
    case Start if settings.testMode => println("test mode on networker")
    case _ =>
  }

  def ready(send: ActorRef): Receive = {
    case msg: String => send ! Udp.Send(ByteString(msg), receiver)
  }
}

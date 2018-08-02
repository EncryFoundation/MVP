package mvp.actors

import java.net.InetSocketAddress
import akka.actor.{Actor, ActorRef, Props}
import akka.io.{IO, Udp}
import akka.util.ByteString
import mvp.MVP.settings
import mvp.actors.Messages.Start

class Sender extends Actor {

  import context.system

  IO(Udp) ! Udp.SimpleSender

  val remote: InetSocketAddress = new InetSocketAddress(settings.thisNode.host, settings.thisNode.port)
  println(remote)

  override def receive: Receive = {
    case Udp.SimpleSenderReady => context.become(ready(sender()))
    case Start if settings.testMode => println("test mode on sender")
    case _ =>
  }

  def ready(send: ActorRef): Receive = {
    case msg: String =>
      println(send.path)
      send ! Udp.Send(ByteString(msg), remote)
  }
}

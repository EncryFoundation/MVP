package mvp.actors

import java.net.InetSocketAddress
import akka.actor.{Actor, ActorRef}
import akka.io.{IO, UdpConnected}
import akka.util.ByteString
import mvp.MVP.settings
import mvp.actors.Messages.Start

class NewClass extends Actor {

  import context.system

  val remote: InetSocketAddress = new InetSocketAddress(settings.thisNode.host, settings.thisNode.port)
  println(remote)

  IO(UdpConnected) ! UdpConnected.Connect(self, remote)

  override def receive: Receive = {
    case UdpConnected.Connected => context.become(ready(sender()))
    case Start if settings.testMode => println("test mode on sender")
  }

  def ready(connection: ActorRef): Receive = {
    case UdpConnected.Received(data) => println(data.toString())
    case Start if settings.testMode => println("test mode on sender")
    case msg: String => connection ! UdpConnected.Send(ByteString(msg))
    case UdpConnected.Disconnect => connection ! UdpConnected.Disconnect
    case UdpConnected.Disconnected ⇒ context.stop(self)
  }
}

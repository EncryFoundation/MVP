package mvp.actors.networkactors

import java.net.InetSocketAddress
import akka.actor.{Actor, ActorRef}
import akka.io.{IO, Udp}
import akka.util.ByteString
import com.typesafe.scalalogging.StrictLogging
import mvp.utils.Settings.settings

class UDPActor extends Actor with StrictLogging {

  import context.system

  val remote: InetSocketAddress = new InetSocketAddress("localhost", settings.otherNodes.head.port)

  override def preStart(): Unit = {
    IO(Udp) ! Udp.Bind(self, new InetSocketAddress("localhost", settings.thisNode.port))

  }

  override def receive: Receive = {
    case Udp.Bound(local) =>
      logger.info(s"Binded to $local")
      context.become(ready(sender))
    case msg => logger.warn(s"Received message $msg from $sender before binding")
  }

  def ready(socket: ActorRef): Receive = {
    case MessageToSend(data) =>
      logger.info(s"Sending $data to $remote (this is ${settings.thisNode.port})")
      socket ! Udp.Send(data, remote)
    case Udp.Received(data: ByteString, remote: InetSocketAddress) =>
      logger.info(s"Received ${data.toString} from $remote")
      context.actorSelection("/user/starter/networker/receiver") ! ReceivedMessage(data)
    case Udp.Unbind =>
      logger.info("Unbind")
      socket ! Udp.Unbind
    case Udp.Unbound =>
      logger.info("Unbound")
      context.stop(self)
  }
}


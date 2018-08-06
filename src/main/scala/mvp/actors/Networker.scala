package mvp.actors

import java.net.InetSocketAddress
import akka.actor.{Actor, Props}
import akka.util.ByteString
import com.typesafe.scalalogging.StrictLogging
import mvp.MVP.{settings, system}
import mvp.actors.Messages.Start
import mvp.modifiers.blockchain.{Header, Payload}
import mvp.modifiers.mempool.Transaction

class Networker extends Actor with StrictLogging{

  override def preStart(): Unit = super.preStart()

  override def receive: Receive = {
    case Start if settings.testMode =>
      logger.info("test mode on networker")
      val receiver = context.actorOf(Props[Receiver].withDispatcher("net-dispatcher").withMailbox("net-mailbox"), "receiver")
      //system.actorSelection("/user/starter/networker/receiver") ! Start
      receiver ! Start
      val sender = context.actorOf(Props[Sender].withDispatcher("net-dispatcher").withMailbox("net-mailbox"), "sender")
      //system.actorSelection("/user/starter/networker/sender") ! Start
      sender ! Start
    case data: ByteString => logger.info(data.toString())
    case remote: InetSocketAddress => logger.info(remote.toString)
    case _ =>
  }

  override def postStop(): Unit = super.postStop()
}

object Networker {

  case class Headers(headers: Seq[Header])

  case class Payloads(payloads: Seq[Payload])

  case class Transactions(transaction: Seq[Transaction])
}
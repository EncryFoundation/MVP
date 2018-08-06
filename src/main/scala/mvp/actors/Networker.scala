package mvp.actors

import java.net.InetSocketAddress
import akka.actor.{Actor, Props}
import akka.util.ByteString
import mvp.MVP.{settings, system}
import mvp.actors.Messages.Start

class Networker extends Actor {

  override def preStart(): Unit = super.preStart()

  override def receive: Receive = {
    case Start if settings.testMode =>
      println("test mode on networker")
      val receiver = context.actorOf(Props[Receiver].withDispatcher("net-dispatcher").withMailbox("net-mailbox"), "receiver")
      //system.actorSelection("/user/starter/networker/receiver") ! Start
      receiver ! Start
      val sender = context.actorOf(Props[Sender].withDispatcher("net-dispatcher").withMailbox("net-mailbox"), "sender")
      //system.actorSelection("/user/starter/networker/sender") ! Start
      sender ! Start
    case data: ByteString => println(data)
    case remote: InetSocketAddress => println(remote)
    case _ =>
  }

  override def postStop(): Unit = super.postStop()
}
package mvp.actors

import akka.actor.{Actor, Props}
import mvp.MVP.{settings, system}
import mvp.actors.Messages.Start

class Starter extends Actor {
  def receive: PartialFunction[Any, Unit] = {
    case Start if settings.testMode =>
      println("test mode on starter")
      context.actorOf(Props[Networker].withDispatcher("net-dispatcher").withMailbox("net-mailbox"), "networker")
      system.actorSelection("/user/starter/networker") ! Start
    case Start if settings.testMode =>
      println("real life baby on starter")
    case _ =>
  }
}

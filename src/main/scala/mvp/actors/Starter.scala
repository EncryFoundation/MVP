package mvp.actors

import akka.actor.{Actor, Props}
import mvp.MVP.{settings, system}
import mvp.actors.Messages.Start
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global

class Starter extends Actor {
  override def receive: Receive = {
    case Start if settings.testMode =>
      println("test mode on starter")
      val networker = context.actorOf(Props[Networker].withDispatcher("net-dispatcher").withMailbox("net-mailbox"), "networker")
      //system.actorSelection("/user/starter/networker") ! Start
      networker ! Start
    case Start if settings.testMode =>
      println("real life baby on starter")
    case _ =>
  }

  context.system.scheduler
    .schedule(initialDelay = 5 seconds, interval = 20 seconds)(system.actorSelection("/user/starter/networker/sender") ! "hello")
}

package mvp.actors

import akka.actor.{Actor, ActorRef, Props}
import mvp.MVP.settings
import mvp.actors.Messages.{Heartbeat, Start}
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global

class Starter extends Actor {

  override def preStart(): Unit = {
    context.system.scheduler.schedule(initialDelay = 10 seconds, interval = settings.heartbeat seconds)(self ! Heartbeat)
  }

  override def receive: Receive = {
    case Start if settings.testMode =>
      println("test mode on starter")
      val networker: ActorRef =
        context.actorOf(Props[Networker].withDispatcher("net-dispatcher").withMailbox("net-mailbox"), "networker")
      //system.actorSelection("/user/starter/networker") ! Start
      networker ! Start
    case Start if settings.testMode =>
      println("real life baby on starter")
    case Heartbeat =>
      println("heartbeat pong")
    case _ =>
  }

  override def postStop(): Unit = super.postStop()

}

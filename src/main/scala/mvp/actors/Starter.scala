package mvp.actors

import com.typesafe.scalalogging.StrictLogging
import akka.actor.{Actor, ActorRef, Props}
import mvp.MVP.settings
import mvp.actors.Messages.{Heartbeat, Start}
import mvp.utils.HttpServer

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global

class Starter extends Actor with StrictLogging{

  override def preStart(): Unit = {
    context.system.scheduler.schedule(initialDelay = 10 seconds, interval = settings.heartbeat seconds)(self ! Heartbeat)
  }

  override def receive: Receive = {
    case Start if settings.testMode =>
      logger.info("test mode on starter")
      val networker: ActorRef =
        context.actorOf(Props[Networker].withDispatcher("net-dispatcher").withMailbox("net-mailbox"), "networker")
      //system.actorSelection("/user/starter/networker") ! Start
      networker ! Start
    case Start if settings.testMode =>
      logger.info("real life baby on starter")
    case Heartbeat =>
      logger.info("heartbeat pong")
      HttpServer.request()
    case _ =>
  }

  override def postStop(): Unit = super.postStop()

}

package mvp.actors

import akka.actor.{Actor, ActorRef, Props}
import com.typesafe.scalalogging.StrictLogging
import mvp.MVP.{settings, system}
import mvp.actors.Messages.{Heartbeat, Start}
import mvp.actors.networkactors.{HttpActor, InfluxActor, Networker}
import mvp.cli.ConsoleActor
import mvp.cli.ConsoleActor._
import mvp.http.HttpServer
import mvp.utils.EncodingUtils._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

class Starter extends Actor with StrictLogging {

  override def preStart(): Unit = {
    context.system.scheduler.schedule(initialDelay = 1 seconds, interval = settings.heartbeat seconds)(self ! Heartbeat)
  }

  override def receive: Receive = {
    case Start if settings.testMode =>
      logger.info("test mode on starter")
      bornKids()
    case Start if !settings.testMode => logger.info("real life baby on starter")
    case Heartbeat => context.actorSelection("/user/starter/networker") ! Heartbeat
    case _ =>
  }

  override def postStop(): Unit = super.postStop()

  def bornKids(): Unit = {
    context.actorOf(Props[Networker].withDispatcher("net-dispatcher").withMailbox("net-mailbox"), "networker")
    system.actorOf(Props[StateHolder], "stateHolder")
    HttpServer.start
    context.actorOf(Props[Zombie].withDispatcher("common-dispatcher"), "zombie")
    if (settings.mvpSettings.enableCLI) {
      context.actorOf(Props[ConsoleActor].withDispatcher("common-dispatcher"), "cliActor")
      consoleListener
    }
    if (settings.mvpSettings.sendStat)
      context.actorOf(Props[InfluxActor].withDispatcher("common-dispatcher"), "influxActor")
    if (settings.levelDB.enable)
      context.actorOf(Props[ModifiersHolder].withDispatcher("common-dispatcher"), "modifiersHolder")
  }
}

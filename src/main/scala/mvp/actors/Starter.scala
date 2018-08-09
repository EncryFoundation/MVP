package mvp.actors

import com.typesafe.scalalogging.StrictLogging
import akka.actor.{Actor, ActorRef, Props}
import mvp.MVP.{materializer, settings}
import mvp.actors.Messages.{Heartbeat, Start}
import mvp.utils.{Data, HttpServer}
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import mvp.stats.InfluxActor
import mvp.stats.InfluxActor._

class Starter extends Actor with StrictLogging {

  override def preStart(): Unit = {
    context.system.scheduler.schedule(initialDelay = 10 seconds, interval = settings.heartbeat seconds)(self ! Heartbeat)
  }

  override def receive: Receive = {
    case Start if settings.testMode =>
      logger.info("test mode on starter")
      bornKids()
    case Start if !settings.testMode => logger.info("real life baby on starter")
    case Heartbeat =>
      logger.info("heartbeat pong")
      if (settings.mvpSettings.sendStat) context.actorSelection("/user/starter/influxActor") ! CurrentBlockHeight()
      HttpServer.request().onComplete {
        case Success(res) =>
          val result: String = res.entity.toStrict(1 second)(materializer).toString
          def parse(data: String): Data = Data(List.empty, List.empty, List.empty) //TODO
        val parsedResult: Data = parse(result)
        case Failure(_) =>
      }
    case _ =>
  }

  override def postStop(): Unit = super.postStop()

  def bornKids(): Unit = {
    val networker: ActorRef =
      context.actorOf(Props[Networker].withDispatcher("net-dispatcher").withMailbox("net-mailbox"), "networker")
    networker ! Start
    context.actorOf(Props[Zombie].withDispatcher("common-dispatcher"), "zombie")
    if (settings.mvpSettings.sendStat) context.actorOf(Props[InfluxActor].withDispatcher("common-dispatcher"), "influxActor")
  }

}

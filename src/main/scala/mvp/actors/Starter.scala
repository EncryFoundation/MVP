package mvp.actors

import com.typesafe.scalalogging.StrictLogging
import akka.actor.{Actor, ActorRef, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.Host
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import akka.util.ByteString
import mvp.MVP.{materializer, settings, system}
import mvp.actors.Messages.{Heartbeat, Start}
import mvp.modifiers.blockchain.Block
import io.circe.parser.decode
import mvp.actors.StateHolder.{Headers, Payloads}
import mvp.http.HttpServer
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global
import mvp.stats.InfluxActor
import scala.concurrent.Future

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
      Http().singleRequest(HttpRequest(
        method = HttpMethods.GET,
        uri = "/blockchain/lastBlock"
      ).withEffectiveUri(securedConnection = false, Host(settings.otherNodes.head.host,settings.otherNodes.head.port)))
        .flatMap(_.entity.dataBytes.runFold(ByteString.empty)(_ ++ _))
        .map(_.utf8String)
        .map(decode[Block])
        .flatMap(_.fold(Future.failed, Future.successful))
        .onComplete(_.map { block =>
          context.system.actorSelection("user/stateHolder") ! Headers(Seq(block.header))
          context.system.actorSelection("user/stateHolder") ! Payloads(Seq(block.payload))
        })
    case _ =>
  }

  override def postStop(): Unit = super.postStop()

  def bornKids(): Unit = {
    val networker: ActorRef =
      context.actorOf(Props[Networker].withDispatcher("net-dispatcher").withMailbox("net-mailbox"), "networker")
    val stateHolder: ActorRef = system.actorOf(Props[StateHolder], "stateHolder")
    HttpServer.start(stateHolder)
    networker ! Start
    context.actorOf(Props[Zombie].withDispatcher("common-dispatcher"), "zombie")
    if (settings.mvpSettings.sendStat)
      context.actorOf(Props[InfluxActor].withDispatcher("common-dispatcher"), "influxActor")
  }
}

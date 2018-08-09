package mvp.actors

import com.typesafe.scalalogging.StrictLogging
import akka.actor.{Actor, ActorRef, Props}
import mvp.MVP.{context, materializer, settings}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.Host
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import akka.util.ByteString
import mvp.MVP.{materializer, settings, system}
import mvp.actors.Messages.{Heartbeat, Start}
import io.circe.parser.decode
import mvp.actors.StateHolder.{Headers, Message, Payloads}
import mvp.http.HttpServer

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import mvp.cli.{ConsoleActor, Response}
import mvp.stats.InfluxActor
import mvp.stats.InfluxActor._
import mvp.cli.ConsoleActor._
import mvp.modifiers.blockchain.Header
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
      println("heartbeat pong")
      Http().singleRequest(HttpRequest(
        method = HttpMethods.GET,
        uri = "/blockchain/lastInfo"
      ).withEffectiveUri(securedConnection = false, Host(settings.otherNodes.head.host,settings.otherNodes.head.port)))
        .flatMap(_.entity.dataBytes.runFold(ByteString.empty)(_ ++ _))
        .map(_.utf8String)
        .map(decode[LastInfo])
        .flatMap(str => {
          println(str)
          str.fold(Future.failed, Future.successful)
        })
        .onComplete(_.map { lastInfo =>
          println(s"Get header: ${Header.jsonEncoder(lastInfo.blocks.head.header)}")
          context.system.actorSelection("user/stateHolder") ! Headers(lastInfo.blocks.map(_.header))
          context.system.actorSelection("user/stateHolder") ! Payloads(lastInfo.blocks.map(_.payload))
          lastInfo.messages.foreach(message =>
            context.system.actorSelection("user/stateHolder") ! Message(message)
          )
        })
    case _ =>
  }

  override def postStop(): Unit = super.postStop()

  def bornKids(): Unit = {
    val networker: ActorRef =
      context.actorOf(Props[Networker].withDispatcher("net-dispatcher").withMailbox("net-mailbox"), "networker")
    system.actorOf(Props[StateHolder], "stateHolder")
    HttpServer.start
    networker ! Start
    context.actorOf(Props[Zombie].withDispatcher("common-dispatcher"), "zombie")
    if (settings.mvpSettings.enableCLI) {
      context.actorOf(Props[ConsoleActor].withDispatcher("common-dispatcher"), "cliActor")
      consoleListener
    }
    if (settings.mvpSettings.sendStat) context.actorOf(Props[InfluxActor].withDispatcher("common-dispatcher"), "influxActor")
    if (settings.mvpSettings.sendStat)
      context.actorOf(Props[InfluxActor].withDispatcher("common-dispatcher"), "influxActor")
  }
}

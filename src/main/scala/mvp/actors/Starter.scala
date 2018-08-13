package mvp.actors

import akka.actor.{Actor, ActorRef, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.Host
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import akka.util.ByteString
import com.typesafe.scalalogging.StrictLogging
import io.circe.parser.decode
import mvp.MVP.{materializer, settings, system}
import mvp.actors.Messages.{Heartbeat, Start, Headers, Payloads}
import mvp.cli.ConsoleActor
import mvp.cli.ConsoleActor._
import mvp.http.HttpServer
import mvp.local.messageHolder.UserMessage
import mvp.modifiers.blockchain.Block
import mvp.stats.InfluxActor
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
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
    case Heartbeat =>
      logger.info("heartbeat pong")
      Http().singleRequest(HttpRequest(
        method = HttpMethods.GET,
        uri = "/blockchain/lastInfo"
      ).withEffectiveUri(securedConnection = false, Host(settings.otherNodes.head.host, settings.otherNodes.head.port)))
        .flatMap(_.entity.dataBytes.runFold(ByteString.empty)(_ ++ _))
        .map(_.utf8String)
        .map(decode[LastInfo])
        .flatMap(res => res.fold(Future.failed, Future.successful))
        .onComplete(_.map { lastInfo =>
          logger.info(s"Get blocks from remote: ${lastInfo.blocks.map(block => Block.jsonEncoder(block)).mkString("\n")}")
          logger.info(s"Get messages from remote: ${lastInfo.messages.map(msg => UserMessage.jsonEncoder(msg)).mkString("\n")}")
          context.system.actorSelection("user/stateHolder") ! Headers(lastInfo.blocks.map(_.header))
          context.system.actorSelection("user/stateHolder") ! Payloads(lastInfo.blocks.map(_.payload))
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
    if (settings.mvpSettings.sendStat)
      context.actorOf(Props[InfluxActor].withDispatcher("common-dispatcher"), "influxActor")
    if (settings.levelDB.enable)
      context.actorOf(Props[ModifiersHolder].withDispatcher("common-dispatcher"), "modifiersHolder")
  }
}

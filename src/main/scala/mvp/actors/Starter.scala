package mvp.actors

import com.typesafe.scalalogging.StrictLogging
import akka.actor.{Actor, ActorRef, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.Host
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.util.ByteString
import mvp.MVP.{materializer, settings, system}
import mvp.actors.Messages.{Heartbeat, Start}
import mvp.modifiers.blockchain.Block
import io.circe.parser.decode

import scala.util.{Failure, Success}
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
//import scala.util.{Failure, Success}

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
      ).withEffectiveUri(securedConnection = false, Host(s"http://${settings.otherNodes.head.host}:${settings.otherNodes.head.port}")))
        .flatMap(_.entity.dataBytes.runFold(ByteString.empty)(_ ++ _))
        .map(_.utf8String)
        .map(decode[Block])
        .flatMap(_.fold(Future.failed, Future.successful))
        .onComplete(block => println(s"Block: ${block.get}"))
    case _ =>
  }

  override def postStop(): Unit = super.postStop()

  def bornKids(): Unit = {
    val networker: ActorRef =
      context.actorOf(Props[Networker].withDispatcher("net-dispatcher").withMailbox("net-mailbox"), "networker")
    networker ! Start
    context.actorOf(Props[Zombie].withDispatcher("common-dispatcher"), "zombie")
  }

}

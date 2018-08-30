package mvp.actors

import akka.actor.Actor
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.Host
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import akka.util.ByteString
import com.typesafe.scalalogging.StrictLogging
import mvp.MVP._
import mvp.actors.Messages.{Headers, Heartbeat, Payloads, Start}
import mvp.utils.EncodingUtils._
import io.circe.parser.decode
import io.circe.syntax._
import io.circe.generic.auto._
import scala.concurrent.{ExecutionContextExecutor, Future}

class HttpNetworker extends Actor with StrictLogging {

  implicit private val ec: ExecutionContextExecutor = context.system.dispatcher

  override def receive: Receive = {
    case Start if settings.testMode =>
      logger.info("test mode on udp networker")
    case Heartbeat =>
      logger.info("heartbeat pong")
      Http().singleRequest(HttpRequest(
        method = HttpMethods.GET,
        uri = "/blockchain/lastInfo"
      ).withEffectiveUri(securedConnection = false, Host(settings.otherNodes.head.host, settings.otherNodes.head.port)))
        .flatMap(_.entity.dataBytes.runFold(ByteString.empty)(_ ++ _))
        .map(_.utf8String)
        .map(decode[LastInfo])
        .flatMap(_.fold(Future.failed, Future.successful))
        .onComplete(_.map { lastInfo =>
          logger.info(s"Get blocks from remote: ${lastInfo.blocks.map(_.asJson).mkString("\n")}")
          logger.info(s"Get messages from remote: ${lastInfo.messages.map(_.asJson).mkString("\n")}")
          context.system.actorSelection("user/stateHolder") ! Headers(lastInfo.blocks.map(_.header))
          context.system.actorSelection("user/stateHolder") ! Payloads(lastInfo.blocks.map(_.payload))
        })
  }

}

package mvp.actors.networkactors

import akka.actor.Actor
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.Host
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import akka.util.ByteString
import com.typesafe.scalalogging.StrictLogging
import io.circe.generic.auto._
import io.circe.parser.decode
import mvp.actors.networkactors.NetworkMessages.RequestLastInfo
import mvp.MVP._
import mvp.actors.LastInfo
import mvp.utils.EncodingUtils._
import scala.concurrent.{ExecutionContextExecutor, Future}
import mvp.utils.Settings.settings

class HttpActor extends Actor with StrictLogging {

  implicit private val ec: ExecutionContextExecutor = context.system.dispatcher

  override def receive: Receive = {
    case RequestLastInfo =>
      logger.info("heartbeat pong")
      Http().singleRequest(HttpRequest(
        method = HttpMethods.GET,
        uri = "/blockchain/lastInfo"
      ).withEffectiveUri(securedConnection = false, Host(settings.otherNodes.head.host, settings.otherNodes.head.port)))
        .flatMap(_.entity.dataBytes.runFold(ByteString.empty)(_ ++ _))
        .map(str => str.utf8String)
        .map(decode[LastInfo])
        .flatMap(res => res.fold(Future.failed, Future.successful))
        .onComplete(_.map(context.parent ! LastInfoFromRemote(_)))
  }

}

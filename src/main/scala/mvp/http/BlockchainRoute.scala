package mvp.http

import akka.actor.ActorRefFactory
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import io.circe.Json
import io.circe.syntax._
import io.circe.generic.auto._
import mvp.actors.LastInfo
import mvp.actors.Messages.GetLastInfo
import mvp.utils.EncodingUtils._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}

case class BlockchainRoute(implicit val context: ActorRefFactory) {

  implicit val ec: ExecutionContextExecutor = context.dispatcher

  implicit val timeout: Timeout = Timeout(10.seconds)

  protected def toJsonResponse(fn: Future[Json]): Route = onSuccess(fn)
  { json => complete(HttpEntity(ContentTypes.`application/json`, json.spaces2)) }

  val route: Route = pathPrefix("blockchain") {
    lastBlock
  }

  private def getLastInfo: Future[LastInfo] = (context.actorSelection("user/stateHolder") ? GetLastInfo).mapTo[LastInfo]

  def lastBlock: Route = pathPrefix("lastInfo") {
    toJsonResponse(getLastInfo.map(_.asJson))
  }
}

package mvp.http.routes

import akka.actor.ActorRefFactory
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout
import io.circe.Json
import io.circe.syntax._
import mvp.actors.StateHolder.GetLastBlock
import mvp.modifiers.blockchain.Block
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

  private def getLastBlock: Future[Block] = (context.actorSelection("user/stateHolder") ? GetLastBlock).mapTo[Block]

  def lastBlock: Route = pathPrefix("lastBlock") {
    toJsonResponse(getLastBlock.map(_.asJson))
  }
}

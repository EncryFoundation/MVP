package mvp.http.routes

import akka.actor.{ActorRef, ActorRefFactory}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.pattern.ask
import akka.http.scaladsl.server.{Directives, Route}
import akka.util.Timeout
import io.circe.Json
import io.circe.syntax._
import mvp.actors.StateHolder.GetLastBlock
import mvp.modifiers.blockchain.Block
import scala.concurrent.duration._

import scala.concurrent.{ExecutionContextExecutor, Future}

case class BlockchainRoute(stateHolder: ActorRef)(implicit val context: ActorRefFactory) extends Directives {

  implicit val ec: ExecutionContextExecutor = context.dispatcher

  implicit val timeout: Timeout = Timeout(10.seconds)

  protected def toJsonResponse(fn: Future[Json]): Route = onSuccess(fn)
  { json => complete(HttpEntity(ContentTypes.`application/json`, json.spaces2)) }

  val route: Route = pathPrefix("history") {
    lastBlock
  }

  private def getLastBlock: Future[Block] = (stateHolder ? GetLastBlock).mapTo[Block]

  def lastBlock: Route = pathPrefix("lastBlock") {
    toJsonResponse(getLastBlock.map(_.asJson))
  }

}

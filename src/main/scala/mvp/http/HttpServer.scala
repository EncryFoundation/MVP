package mvp.http

import akka.actor.ActorRef
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import mvp.MVP.{settings, system}
import mvp.http.routes.BlockchainRoute

import scala.concurrent.ExecutionContextExecutor

object HttpServer {

  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val context: ExecutionContextExecutor = system.dispatcher

  def start(stateHolder: ActorRef): Unit = {
    val route : Route = BlockchainRoute(stateHolder).route
    Http().bindAndHandle(route, settings.thisNode.host, settings.thisNode.port)
  }
}

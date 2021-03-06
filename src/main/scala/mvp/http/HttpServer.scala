package mvp.http

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import mvp.MVP.system
import mvp.utils.Settings.settings
import scala.concurrent.ExecutionContextExecutor

object HttpServer {

  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val context: ExecutionContextExecutor = system.dispatcher

  def start(): Unit = {
    val route : Route = BlockchainRoute().route
    Http().bindAndHandle(route, settings.thisNode.host, settings.thisNode.port)
  }
}
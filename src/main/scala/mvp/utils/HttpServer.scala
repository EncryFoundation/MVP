package mvp.utils

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.scaladsl.Sink
import akka.http.scaladsl.model.HttpMethods._
import scala.concurrent.Future
import mvp.MVP._
import scala.util.{Failure, Success}
import scala.concurrent.duration._

object HttpServer {

  def start(): Future[Http.ServerBinding] =
    Http().bind(settings.thisNode.host, settings.thisNode.port).to(Sink.foreach { connection =>
      println("New request from " + connection.remoteAddress)
      connection handleWithSyncHandler requestHandler
    }).run()

  val requestHandler: HttpRequest => HttpResponse = {
    case HttpRequest(GET, Uri.Path("/"), _, _, _) => HttpResponse(entity = HttpEntity(
      ContentTypes.`application/json`,
      s"""{"system": "test", "message":"ready"}"""))
    case _ => HttpResponse(entity = HttpEntity(
      ContentTypes.`application/json`,
      s"""{"system": "test", "message":"moloko"}"""))
  }

  def request(): Unit = {
    val uri = s"http://localhost:${settings.otherNodes.head.port}/"
    val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = uri))
    responseFuture.onComplete {
      case Success(res) => system.log.info(res.entity.toStrict(1 second)(materializer).toString)
      case Failure(_) =>
    }
  }
}

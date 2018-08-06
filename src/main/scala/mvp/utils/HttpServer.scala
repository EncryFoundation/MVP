package mvp.utils

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.scaladsl.Sink
import akka.http.scaladsl.model.HttpMethods._
import com.typesafe.scalalogging.StrictLogging
import scala.concurrent.Future
import mvp.MVP._
import scala.util.{Failure, Success}
import scala.concurrent.duration._
import scala.language.postfixOps

object HttpServer extends StrictLogging {

  def start(): Future[Http.ServerBinding] =
    Http().bind(settings.thisNode.host, settings.thisNode.port).to(Sink.foreach { connection =>
      logger.info("New request from " + connection.remoteAddress)
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

  def request(): Future[HttpResponse] = Http().singleRequest(HttpRequest(
    uri = s"http://${settings.otherNodes.head.port}:${settings.otherNodes.head.port}/"))


}

case class Data(blocks: List[Block], txs: List[Tx], nodes: List[Node])

case class Tx(timestamp: Long)

case class Block(header: Header, payload: Payload)

case class Header(timestamp: Long, previousBlockHash: Array[Byte], minerSignature: Array[Byte], merkleTreeRoot: Array[Byte])

case class Payload(transactions: Seq[Tx])
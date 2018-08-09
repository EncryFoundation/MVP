package mvp.cli.commands

import mvp.actors.StateHolder
import mvp.cli.Response
import mvp.utils.Settings
import akka.util.Timeout
import scala.concurrent.Future
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

class CurrentHight  {
  import CurrentHight._
  implicit val timeout: Timeout = Timeout(5.seconds)

//  override def execute(args: Command.Args, settings: Settings): Future[Option[Response]] = {
//    (context. ?
//      requestHight { message =>
//        Some(Response(message.blockchain.headerHeight))
//      })
//  }
}

object CurrentHight {
  case class requestHight()
}

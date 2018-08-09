package mvp.cli.commands

import mvp.MVP
import mvp.cli.Response
import mvp.utils.Settings
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object NodeShutdown extends Command {

  override def execute(args: Command.Args, settings: Settings): Future[Option[Response]] = {
    MVP.forceStopApplication()
    Future(None)
  }
}

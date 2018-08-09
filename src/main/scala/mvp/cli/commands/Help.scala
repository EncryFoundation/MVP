package mvp.cli.commands

import mvp.cli.Response
import mvp.utils.Settings
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Help extends Command {

  override def execute(args: Command.Args, settings: Settings): Future[Option[Response]] =
    Future(Some(Response(
      """
        |Usage: [GROUP_NAME] [COMMAND] -[ARGUMENT_1]=[VAL_1] -[ARGUMENT_2]=[VAL_2]
        |
        |Group name    Command          Argument       Meaning
        |--------------------------------------------------------------------------------
        |node          shutdown         None           Shutdown the node
        |app           help             None           Show all supported commands
      """
        .stripMargin)))
}
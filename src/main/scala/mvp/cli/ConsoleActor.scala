package mvp.cli

import akka.actor.Actor
import mvp.MVP._
import mvp.cli.commands.{Command, Help, NodeShutdown}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class ConsoleActor extends Actor {

  import ConsoleActor._

  override def receive: Receive = {
    case StartListening =>
      Iterator.continually(scala.io.StdIn.readLine(prompt)).foreach { input =>
        InputParser.parse(input) match {
          case Success(command) =>
            getCommand(command.category.name, command.ident.name) match {
              case Some(cmd) =>
                cmd.execute(Command.Args(command.params.map(p => p.ident.name -> p.value).toMap), settings)
                  .map {
                    case Some(x) => print(x.msg + s"\n$prompt")
                    case None =>
                  }
              case None =>
            }
          case Failure(_) =>
        }
      }
  }
}

object ConsoleActor {

  case object StartListening

  val prompt = "$>"

  def getCommand(cat: String, cmd: String): Option[Command] = cmdDictionary.get(cat).flatMap(_.get(cmd))

  private val nodeCmds = Map("node" -> Map(
    "shutdown" -> NodeShutdown
  ))

  private val appCmds = Map("app" -> Map(
    "help" -> Help
  ))

  val cmdDictionary: Map[String, Map[String, Command]] =
    ConsoleActor.nodeCmds ++ ConsoleActor.appCmds
}

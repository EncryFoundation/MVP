package mvp.cli.commands

import mvp.cli.{Ast, Response}
import mvp.utils.Settings
import scala.concurrent.Future

trait Command {

  def execute(args: Command.Args, settings: Settings): Future[Option[Response]]

}

object Command {

  case class Args(args: Map[String, Ast.Value]) {

    def requireArg[VT <: Ast.Value](n: String): VT = args.get(n).map {
      case vt: VT@unchecked => vt
      case _ => throw new Exception("Wrong argument type.")
    }.getOrElse(throw new Exception(s"Argument $n not found."))

    def requireArgOrElse[VT <: Ast.Value](key: String, default: => VT): VT = args.get(key).map {
      case vt: VT@unchecked => vt
      case _ => default
    }.getOrElse(default)
  }

}

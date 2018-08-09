package mvp.cli

import scala.io.StdIn
import scala.concurrent.Future
import akka.actor.Actor
import mvp.MVP.{context, system}
import mvp.cli.Commands._

class ConsoleActor extends Actor {

  override def receive: Receive = {
    case Response("app help") => showHelp
    case Response("node shutdown") => nodeShutdown()
    case Response(_) =>
  }
}

object ConsoleActor {

  def consoleListener = Future {
    Iterator.continually(StdIn.readLine("$> ")).foreach(input =>
      system.actorSelection("/user/starter/cliActor") ! Response(input))
  }
}

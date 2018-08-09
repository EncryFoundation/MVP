package mvp.cli

import scala.io.StdIn
import scala.concurrent.Future
import akka.actor.Actor
import mvp.MVP.{context, system}
import mvp.actors.StateHolder.{BlockchainAnswer, HeadersAnswer}
import mvp.cli.Commands._
import mvp.cli.ConsoleActor.{BlockchainRequest, HeadersRequest}

class ConsoleActor extends Actor {

  override def receive: Receive = {
    case Response("app help") => showHelp
    case Response("node shutdown") => nodeShutdown()
    case Response("blockchain height") => system.actorSelection("/user/stateHolder") ! BlockchainRequest
    case Response("headers height") => system.actorSelection("/user/stateHolder") ! HeadersRequest
    case BlockchainAnswer(blockchain) => showCurrentBlockchainHight(blockchain)
    case HeadersAnswer(blockchain) => showCurrentHeadersHight(blockchain)
    case Response(_) => println("bad request")
  }
}

object ConsoleActor {

  def consoleListener = Future {
    Iterator.continually(StdIn.readLine("$> ")).foreach(input =>
      system.actorSelection("/user/starter/cliActor") ! Response(input))
  }

  case object BlockchainRequest

  case object HeadersRequest
}

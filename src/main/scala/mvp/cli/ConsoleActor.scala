package mvp.cli

import scala.io.StdIn
import scala.concurrent.Future
import akka.actor.Actor
import mvp.MVP.{context, system}
import mvp.actors.StateHolder.{BlockchainAnswer, HeadersAnswer}
import mvp.cli.Commands._
import mvp.cli.ConsoleActor.{BlockchainRequest, HeadersRequest, SendMyName, UserMessageFromCLI}
import scorex.util.encode.Base58

class ConsoleActor extends Actor {

  override def receive: Receive = {
    //    case Response("app help") => showHelp
    //    case Response("node shutdown") => nodeShutdown()
    case Response("blockchain height") => system.actorSelection("/user/stateHolder") ! BlockchainRequest
    case Response("headers height") => system.actorSelection("/user/stateHolder") ! HeadersRequest
    case Response("send my name") => system.actorSelection("/user/stateHolder") ! SendMyName
    case BlockchainAnswer(blockchain) => showCurrentBlockchainHight(blockchain)
    case HeadersAnswer(blockchain) => showCurrentHeadersHight(blockchain)
    case Response("send message") => println("Введите текст и outputID")
//    case Response(string: String) =>
//      val words: Array[String] = string.split('@')
//      if (words.head == "out") {
//        val outputID = if (words.length < 2) None else Some(Base58.decode(words.last).getOrElse(Array.emptyByteArray))
//        system.actorSelection("/user/stateHolder") !
//          UserMessageFromCLI(words., outputID)
//      }
  }
}

object ConsoleActor {

  def consoleListener = Future {
    Iterator.continually(StdIn.readLine("$> ")).foreach(input =>
      system.actorSelection("/user/starter/cliActor") ! Response(input))
  }

  case object SendMyName

  case object BlockchainRequest

  case object HeadersRequest

  case class UserMessageFromCLI(array: Array[String], array1: Option[Array[Byte]])

}

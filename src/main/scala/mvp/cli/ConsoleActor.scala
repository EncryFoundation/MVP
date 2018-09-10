package mvp.cli

import akka.actor.Actor
import akka.util.ByteString
import mvp.MVP.{context, system}
import mvp.actors.Messages.{BlockchainAnswer, HeadersAnswer}
import mvp.cli.Commands._
import mvp.cli.ConsoleActor._
import mvp.utils.Base16
import scala.concurrent.Future
import scala.io.StdIn

class ConsoleActor extends Actor {

  override def receive: Receive = {
    case Response("app help") => showHelp
    case Response("node shutdown") => nodeShutdown()
    case Response("blockchain height") => context.system.actorSelection("/user/stateHolder") ! BlockchainRequest
    case Response("headers height") => context.system.actorSelection("/user/stateHolder") ! HeadersRequest
    case Response("send my name") => context.system.actorSelection("/user/stateHolder") ! SendMyName
    case Response("MyAddr") => context.system.actorSelection("/user/stateHolder") ! MyAddress
    case Response("MyBalance") => context.system.actorSelection("/user/stateHolder") ! MyBalance
    case BlockchainAnswer(blockchain) => showCurrentBlockchainHight(blockchain)
    case HeadersAnswer(blockchain) => showCurrentHeadersHight(blockchain)
    case balance: Long => println(s"Balance: $balance")
    case address: ByteString => println(s"Address: ${Base16.encode(address)}")
    case Response("send message") => println("Введите текст и outputID")
    case Response(string: String) =>
      val words: Array[String] = string.split(' ')
      words.head match {
        case "sendTx" =>
            if (words.length < 3) println("Looks like you miss some parameters, please try again.")
            else system.actorSelection("/user/stateHolder") !
              UserMessageFromCLI(words.tail)
        case "sendMoney" =>
          if (words.length < 4) println("Looks like you miss some parameters, please try again.")
          else
            system.actorSelection("/user/stateHolder") !
              UserTransfer(Base16.decode(words(1)).getOrElse(ByteString.empty), words(2).toLong, words(3).toLong)
      }
  }
}

object ConsoleActor {

  def consoleListener = Future {
    Iterator.continually(StdIn.readLine("$> ")).foreach(input =>
      system.actorSelection("/user/starter/cliActor") ! Response(input))
  }

  case object MyAddress

  case object MyBalance

  case object SendMyName

  case object BlockchainRequest

  case object HeadersRequest

  case class UserMessageFromCLI(messageWithFee: Array[String])

  case class UserTransfer(addr: ByteString, amount: Long, fee: Long)
}
package mvp.actors

import akka.actor.Actor
import mvp.MVP.settings
import mvp.actors.Messages.Start
import mvp.modifiers.blockchain.{Header, Payload}
import mvp.modifiers.mempool.Transaction

class Networker extends Actor {
  def receive: PartialFunction[Any, Unit] = {
    case Start if settings.testMode =>
      println("test mode on networker")
    case _ =>
  }
}

object Networker {

  case class Headers(headers: Seq[Header])

  case class Payloads(payloads: Seq[Payload])

  case class Transactions(transaction: Seq[Transaction])
}
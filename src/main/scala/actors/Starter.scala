package actors

import akka.actor.Actor

class Starter extends Actor {
  def receive: PartialFunction[Any, Unit] = {
    case true =>
    case _ =>
  }
}

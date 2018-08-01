package mvp.actors

import akka.actor.Actor
import mvp.MVP.settings
import mvp.actors.Messages.Start

class Networker extends Actor {
  def receive: PartialFunction[Any, Unit] = {
    case Start if settings.testMode =>
      println("test mode on networker")
    case _ =>
  }
}
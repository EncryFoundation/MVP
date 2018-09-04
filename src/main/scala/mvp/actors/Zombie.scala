package mvp.actors

import akka.actor.{Actor, DeadLetter, UnhandledMessage}
import com.typesafe.scalalogging.StrictLogging
import mvp.MVP.system

class Zombie extends Actor with StrictLogging {

  override def preStart(): Unit = {
    system.eventStream.subscribe(self, classOf[DeadLetter])
    system.eventStream.subscribe(self, classOf[UnhandledMessage])
  }

  override def receive: Receive = {
    case deadMessage: DeadLetter => logger.debug(s"Dead letter: ${deadMessage.toString}.")
    case unhandled: UnhandledMessage => logger.debug(s"Unhandled message ${unhandled.toString}")
  }

}
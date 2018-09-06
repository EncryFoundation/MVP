package mvp

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import mvp.actors.Messages.Start
import mvp.actors.Starter
import mvp.utils.Settings
import scala.concurrent.ExecutionContextExecutor

object MVP extends App {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val context: ExecutionContextExecutor = system.dispatcher

  system.actorOf(Props[Starter], "starter")
  system.actorSelection("/user/starter") ! Start
}
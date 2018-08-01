import actors.Starter
import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import utils.Settings
import scala.concurrent.ExecutionContextExecutor

object MVP extends App {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val context: ExecutionContextExecutor = system.dispatcher

  val settings: Settings = Settings.load

  system.actorOf(Props[Starter], "starter")

}

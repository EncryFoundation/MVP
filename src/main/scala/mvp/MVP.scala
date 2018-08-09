package mvp

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import mvp.actors.Messages.Start
import mvp.actors.Starter
import mvp.utils.{HttpServer, Settings}
import mvp.actors.{Starter, StateHolder}
import mvp.utils.Settings
import akka.http.scaladsl.server.{Directives, Route}
import mvp.http.routes.BlockchainRoute

import scala.concurrent.ExecutionContextExecutor

object MVP extends App {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val context: ExecutionContextExecutor = system.dispatcher

  val settings: Settings = Settings.load

  system.actorOf(Props[Starter], "starter")
  system.actorSelection("/user/starter") ! Start
}

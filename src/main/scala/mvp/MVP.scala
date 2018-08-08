package mvp

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import mvp.actors.Messages.Start
import mvp.actors.{Starter, StateHolder}
import mvp.utils.Settings
import akka.http.scaladsl.server.{Directives, Route}
import mvp.http.routes.BlockchainRoute

import scala.concurrent.ExecutionContextExecutor

object MVP extends App with Directives {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val context: ExecutionContextExecutor = system.dispatcher

  val settings: Settings = Settings.load
  val stateHolder: ActorRef = system.actorOf(Props[StateHolder], "stateHolder")
  system.actorOf(Props[Starter], "starter")
  system.actorSelection("/user/starter") ! Start

  val route : Route = BlockchainRoute(stateHolder).route

  Http().bindAndHandle(route, settings.thisNode.host, settings.thisNode.port)
}

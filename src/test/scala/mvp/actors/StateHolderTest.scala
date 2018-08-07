package mvp.actors

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import mvp.MVP.system
import mvp.modifiers.state.input.Input
import org.scalatest.{Matchers, PropSpec}
import scorex.utils.Random
import utils.TestGenerator._

import scala.concurrent.ExecutionContextExecutor

class StateHolderTest extends PropSpec with Matchers {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val context: ExecutionContextExecutor = system.dispatcher

  property("Dummy blockchain impl") {

    val inputs = generateDummyAmountOutputs(10).map(output => Input(output.id, Random.randomBytes()))

   println(generateBlockChainWithAmountPayloads(10, inputs))
  }
}

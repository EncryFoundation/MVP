package mvp.data

import org.scalatest.{Matchers, WordSpecLike}
import utils.TestGenerator._
import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit}
import akka.util.ByteString
import mvp.actors.StateHolder
import mvp.utils.Base16

class BlockchainSpec extends TestKit(ActorSystem("MySpec")) with WordSpecLike with Matchers {

  val stateHolder: TestActorRef[StateHolder] = TestActorRef(new StateHolder)
  val actor: StateHolder = stateHolder.underlyingActor

  val correctHeader: Header = Header(
    System.currentTimeMillis(),
    1,
    Base16.decode("c6cb5981ea33d2394acd83ebe33b5e3080a09019").get,
    ByteString.empty,
    ByteString.empty
  )

  val incorrectHeader: Header = Header(
    0L,
    1,
    ByteString.fromString("c6cb5981ea33d2394acd83ebe33b5e3080a09019"),
    ByteString.empty,
    ByteString.empty
  )

  val transaction: Transaction = Transaction(
    System.currentTimeMillis(),
    Seq.empty[Input],
    generateDummyAmountOutputs(1)
  )

  val payload: Payload = Payload(
    Seq(transaction)
  )

  assert(actor.validate(correctHeader), "Validate correct header should be true")
  assert(!actor.validate(incorrectHeader), "Validate incorrect header should be false")
  assert(actor.validate(payload), "Validate payload should be true")
  assert(actor.validate(transaction), "Validate transaction should be true")

  shutdown(system)
}
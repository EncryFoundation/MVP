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

  val headerSecond: Header = Header(
    System.currentTimeMillis(),
    1,
    Base16.decode("c6cb5981ea33d2394acd83ebe33b5e3080a09019").get,
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

  assert(actor.validate(headerSecond), "Validate second header")
  assert(actor.validate(payload), "Validate payload should be true")
  assert(actor.validate(transaction), "Validate transaction should be true")

  shutdown(system)
}
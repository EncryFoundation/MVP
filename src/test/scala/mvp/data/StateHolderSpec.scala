package mvp.data

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import akka.util.ByteString
import mvp.actors.StateHolder
import mvp.crypto.ECDSA
import mvp.crypto.Sha256.Sha256RipeMD160
import mvp.local.Generator
import mvp.local.messageHolder.UserMessage
import mvp.utils.Base16
import mvp.utils.Settings.settings
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import utils.TestGenerator._

class StateHolderSpec extends TestKit(ActorSystem("MySpec")) with WordSpecLike
  with ImplicitSender
  with BeforeAndAfterAll
  with Matchers {

  val stateHolder: StateHolder = TestActorRef(new StateHolder).underlyingActor
  var messagesHolder: Seq[UserMessage] = Seq.empty
  val currentSalt: ByteString = ByteString.fromString("00000000000000000000000000000000")
  val timestamp: Long = System.currentTimeMillis()
  var state: State = State.recoverState

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  val correctHeader: Header = Header(
    timestamp,
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
    timestamp,
    Seq.empty[Input],
    generateDummyAmountOutputs(1)
  )

  val payload: Payload = Payload(
    Seq(transaction)
  )

  "validate correct header, payload, transaction , incorrect will be rejected " in {
    assert(stateHolder.validateModifier(correctHeader), "Validate correct header should be true")
    assert(!stateHolder.validateModifier(incorrectHeader), "Validate incorrect header should be false")
    assert(stateHolder.validateModifier(payload), "Validate payload should be true")
    assert(stateHolder.validateModifier(transaction), "Validate transaction should be true")
  }

  val userMessage = UserMessage(
    "",
    ByteString.fromString("4afa0ea465010000"),
    ECDSA.createKeyPair.getPublic,
    Option(ByteString.empty),
    1
  )

  def createMessageTx(message: UserMessage,
                      previousOutput: Option[OutputMessage]): Transaction = {
    messagesHolder = messagesHolder :+ message
    Generator.generateMessageTx(ECDSA.createKeyPair.getPrivate,
      previousOutput.map(_.toProofGenerator),
      previousOutput.map(_.id),
      message,
      previousOutput.map(output =>
        if (output.txNum == 1) settings.mvpSettings.messagesQtyInChain + 1 else output.txNum)
        .getOrElse(settings.mvpSettings.messagesQtyInChain + 1),
      currentSalt
    )
  }

  def addMessageAndCreateTx(msg: UserMessage): Option[Transaction] =
    if (!messagesHolder.contains(msg)) {
      val previousOutput: Option[OutputMessage] =
        state.state.values.toSeq.find {
          case output: OutputMessage if messagesHolder.nonEmpty =>
            output.messageHash ++ output.metadata ++ ByteString(output.publicKey.getEncoded) ==
              Sha256RipeMD160(ByteString(messagesHolder.last.message)) ++
                messagesHolder.last.metadata ++
                ByteString(messagesHolder.last.sender.getEncoded)
          case _ => false
        }.map(_.asInstanceOf[OutputMessage])
      Some(createMessageTx(msg, previousOutput))
    } else None

  "addMessageAndCreateTx should add and create" in {
    val transaction: Option[Transaction] = addMessageAndCreateTx(userMessage)
    assert(stateHolder.validateModifier(transaction.get), "should be valid")
  }

  "create messageTx should create valid transaction" in {
    val state: Int = messagesHolder.size
    assert(stateHolder.validateModifier(createMessageTx(userMessage, None)), "should be true")
    assert(messagesHolder.size > state, "should be true")
  }
}
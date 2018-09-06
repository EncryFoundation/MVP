package mvp.data

import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import utils.TestGenerator._
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import akka.util.ByteString
import mvp.actors.StateHolder
import mvp.local.messageHolder.UserMessage
import mvp.utils.{Base16, Settings}
import mvp.crypto.Curve25519
import mvp.crypto.Sha256.Sha256RipeMD160
import mvp.local.{Generator, Keys}
import mvp.utils.EncodingUtils._
import org.encryfoundation.common.crypto.PrivateKey25519
import scorex.crypto.signatures.{PrivateKey, PublicKey}
import mvp.utils.Settings.settings

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
    assert(stateHolder.validate(correctHeader), "Validate correct header should be true")
    assert(!stateHolder.validate(incorrectHeader), "Validate incorrect header should be false")
    assert(stateHolder.validate(payload), "Validate payload should be true")
    assert(stateHolder.validate(transaction), "Validate transaction should be true")
  }

  val userMessage = UserMessage(
    "",
    ByteString.fromString("4afa0ea465010000"),
    Base16.decode("1477a0c999ad4dbc9d01ff650c8c4a497b0d4e8f829166a149c2939f623b4725").get,
    Option(ByteString.empty),
    1
  )

  def createMessageTx(message: UserMessage,
                      previousOutput: Option[OutputMessage]): Transaction = {
    val (privKeyBytes: ByteString, publicKeyBytes: ByteString) =
      Curve25519.createKeyPair(ByteString.fromString("00000000000000000000000000000000"))
    val keys: Keys = Keys(Seq(PrivateKey25519(PrivateKey @@ privKeyBytes.toArray, PublicKey @@ publicKeyBytes.toArray)))
    messagesHolder = messagesHolder :+ message
    Generator.generateMessageTx(
      keys.keys.head,
      previousOutput.map(_.toProofGenerator),
      previousOutput.map(_.id),
      message,
      previousOutput.map(output =>
        if (output.txNum == 1) settings.mvpSettings.messagesQtyInChain + 1 else output.txNum)
        .getOrElse(settings.mvpSettings.messagesQtyInChain + 1),
      ByteString.fromString("00000000000000000000000000000000")
    )
  }

  def addMessageAndCreateTx(msg: UserMessage): Option[Transaction] =
    if (!messagesHolder.contains(msg)) {
      val previousOutput: Option[OutputMessage] =
        state.state.values.toSeq.find {
          case output: OutputMessage =>
            output.messageHash ++ output.metadata ++ output.publicKey ==
              Sha256RipeMD160(ByteString(messagesHolder.last.message)) ++
                messagesHolder.last.metadata ++
                messagesHolder.last.sender
          case _ => false
        }.map(_.asInstanceOf[OutputMessage])
      Some(createMessageTx(msg, previousOutput))
    } else None

  "addMessageAndCreateTx should add and create" in {
    val transaction: Option[Transaction] = addMessageAndCreateTx(userMessage)
    assert(stateHolder.validate(transaction.get), "should be valid")
  }

  "create messageTx should create valid tramsaction" in {
    val state: Int = messagesHolder.size
    assert(stateHolder.validate(createMessageTx(userMessage, None)), "should be true")
    assert(messagesHolder.size > state, "should be true")
  }
}
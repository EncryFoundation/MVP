package mvp.actors

import akka.actor.Actor
import mvp.actors.StateHolder.{Headers, Payloads, Transactions}
import mvp.modifiers.blockchain.{Header, Payload}
import mvp.modifiers.mempool.Transaction
import mvp.view.blockchain.Blockchain
import mvp.view.blockchain.processor.ModifiersProcessor
import mvp.view.mempool.Mempool
import mvp.view.state.State

class StateHolder extends Actor with ModifiersProcessor {

  var blockChain: Blockchain = Blockchain.recoverBlockchain
  var state: State = State.recoverState
  var mempool: Mempool = Mempool()

  override def receive: Receive = {
    case Headers(headers: Seq[Header]) => headers.filter(validate).foreach(apply)
    case Payloads(payloads: Seq[Payload]) => payloads.filter(validate).foreach(apply)
    case Transactions(transactions: Seq[Transaction]) => transactions.filter(validate).foreach(apply)
  }
}

object StateHolder {

  case class Headers(headers: Seq[Header])

  case class Payloads(payloads: Seq[Payload])

  case class Transactions(transaction: Seq[Transaction])
}
package mvp.actors

import akka.actor.Actor
import mvp.actors.Networker.{Headers, Payloads, Transactions}
import mvp.modifiers.blockchain.{Header, Payload}
import mvp.modifiers.mempool.Transaction
import mvp.view.blockchain.Blockchain
import mvp.view.blockchain.processor.block.{BlockProcessor, HeaderProcessor, PayloadProcessor}
import mvp.view.blockchain.processor.transaction.TransactionProcessor
import mvp.view.state.State

class StateHolder extends Actor with BlockProcessor with HeaderProcessor with PayloadProcessor with TransactionProcessor {

  var blockChain: Blockchain = Blockchain.recoverBlockchain
  var state: State = State.recoverState

  override def receive: Receive = {
    case Headers(headers: Seq[Header]) => headers.filter(validate).foreach(apply)
    case Payloads(payloads: Seq[Payload]) => payloads.filter(validate).foreach(apply)
    case Transactions(transactions: Seq[Transaction]) => transactions.filter(validate)
  }
}

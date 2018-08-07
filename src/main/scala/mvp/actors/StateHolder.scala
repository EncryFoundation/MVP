package mvp.actors

import akka.actor.Actor
import mvp.actors.StateHolder.{Headers, Payloads, Transactions}
import mvp.modifiers.Modifier
import mvp.modifiers.blockchain.{Header, Payload}
import mvp.modifiers.mempool.Transaction
import mvp.modifiers.state.output.{AmountOutput, Output}
import mvp.view.blockchain.Blockchain
import mvp.view.mempool.Mempool
import mvp.view.state.State

class StateHolder extends Actor {

  var blockChain: Blockchain = Blockchain.recoverBlockchain
  var state: State = State.recoverState
  var mempool: Mempool = Mempool()

  def apply(modifier: Modifier): Unit = modifier match {
    case header: Header =>
      blockChain = blockChain.addHeader(header)
    case payload: Payload =>
      blockChain = blockChain.addPayload(payload)
      val (toAddToState, toRemoveFromState) = payload.transactions.foldLeft(Seq.empty[Output] -> Seq.empty[Array[Byte]]){
        case ((toAdd, toRemove), tx) =>
          if (tx.outputs.forall(_.isInstanceOf[AmountOutput])) (toAdd ++ tx.outputs, toRemove ++ tx.inputs.map(_.useOutputId))
          else (toAdd ++ tx.outputs, toRemove)
      }
      state = state.updateState(toAddToState, toRemoveFromState)
    case transaction: Transaction =>
      mempool = mempool.put(Seq(transaction))
  }

  def validate(modifier: Modifier): Boolean = modifier match {
    //TODO: Add semantic validation check
    case header: Header =>
      blockChain.getHeaderAtHeight(header.height - 1).exists(prevHeader => header.previousBlockHash sameElements prevHeader.id)
    case payload: Payload =>
      payload.transactions.forall(validate)
    case transaction: Transaction =>
      transaction.inputs.forall(input => state.state.get(input.useOutputId).exists(outputToUnlock => outputToUnlock.unlock(input.proof)))
  }

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
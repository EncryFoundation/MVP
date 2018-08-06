package mvp.actors

import akka.actor.Actor
import mvp.actors.Networker.Headers
import mvp.modifiers.blockchain.Header
import mvp.view.blockchain.Blockchain
import mvp.view.blockchain.validator.ModifiersValidator
import mvp.view.state.State

class StateHolder extends Actor with ModifiersValidator {

  var blockChain: Blockchain = Blockchain.recoverBlockchain
  var state: State = State.recoverState

  override def receive: Receive = {

    case Headers(headers: Seq[Header]) =>

  }
}

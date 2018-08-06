package mvp.actors

import akka.actor.Actor
import mvp.view.blockchain.Blockchain
import mvp.view.state.State

class StateHolder extends Actor {

  var blockChain: Blockchain = new Blockchain
  var state: State = State.recoverState

  override def receive: Receive = ???
}

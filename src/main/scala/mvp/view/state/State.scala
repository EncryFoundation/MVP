package mvp.view.state

import mvp.modifiers.blockchain.Payload
import mvp.modifiers.state.output.{AmountOutput, Output}
import mvp.utils.Crypto.Sha256RipeMD160

case class State(state: Map[Array[Byte], Output] = Map.empty[Array[Byte], Output]) {

  val stateHash: Array[Byte] = Sha256RipeMD160(state.keys.foldLeft(Array.emptyByteArray)(_ ++ _))

  def updateState(payload: Payload): State = {
    val (toAddToState, toRemoveFromState) = payload.transactions.foldLeft( Seq.empty[Output] -> Seq.empty[Array[Byte]] ) {
      case ((toAdd, toRemove), tx) =>
        if (tx.outputs.forall( _.isInstanceOf[AmountOutput] )) (toAdd ++ tx.outputs, toRemove ++ tx.inputs.map( _.useOutputId ))
        else (toAdd ++ tx.outputs, toRemove)
    }
    State(state = (state ++ toAddToState.map(output => output.id -> output)) -- toRemoveFromState)
  }
}

object State {

  val genesisState: State = State()

  def recoverState: State = {

    //TODO: Add support of levelDb, now always start from empty state
    genesisState
  }
}

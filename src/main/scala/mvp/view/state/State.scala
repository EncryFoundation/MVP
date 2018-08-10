package mvp.view.state

import mvp.modifiers.blockchain.Payload
import mvp.modifiers.state.output.{AmountOutput, Output}
import mvp.utils.Crypto.Sha256RipeMD160
import scorex.util.encode.Base58

case class State(state: Map[String, Output] = Map.empty[String, Output]) {

  val stateHash: Array[Byte] =
    Sha256RipeMD160(state.keys.map(key => Base58.decode(key).get).foldLeft(Array.emptyByteArray)(_ ++ _))

  def updateState(payload: Payload): State = {
    val (toAddToState, toRemoveFromState) = payload.transactions.foldLeft( Seq.empty[Output] -> Seq.empty[Array[Byte]] ) {
      case ((toAdd, toRemove), tx) =>
        if (tx.outputs.forall(_.isInstanceOf[AmountOutput]))
          (toAdd ++ tx.outputs, toRemove ++ tx.inputs.map( _.useOutputId))
        else (toAdd ++ tx.outputs, toRemove)
    }
    State(state = (state ++ toAddToState.map(output => Base58.encode(output.id) -> output)) -- toRemoveFromState.map(Base58.encode))
  }
}

object State {

  val genesisState: State = State()

  //TODO: Add support of levelDb, now always start from empty state
  def recoverState: State = genesisState
}

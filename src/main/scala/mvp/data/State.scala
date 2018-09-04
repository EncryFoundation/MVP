package mvp.data

import akka.util.ByteString
import mvp.crypto.Sha256.Sha256RipeMD160
import mvp.utils.Base16._

case class State(state: Map[String, Output] = Map.empty[String, Output]) {

  val stateHash: ByteString =
    Sha256RipeMD160(
      state.keys.map(key => decode(key).getOrElse(ByteString.empty)).foldLeft(ByteString.empty)(_ ++ _)
    )

  def updateState(payload: Payload): State = {
    val (toAddToState, toRemoveFromState) = payload.transactions.foldLeft(Seq.empty[Output] -> Seq.empty[String]) {
      case ((toAdd, toRemove), tx) =>
        if (tx.outputs.forall(_.isInstanceOf[OutputAmount]))
          (toAdd ++ tx.outputs, toRemove ++ tx.inputs.map(input => encode(input.useOutputId)))
        else (toAdd ++
          tx.outputs ++
          tx.inputs.flatMap(input => state.get(encode(input.useOutputId)).map(_.closeForSpent)),
          toRemove ++ tx.inputs.map(input => encode(input.useOutputId)))
    }
    State(state = (state -- toRemoveFromState) ++ toAddToState.map(output => encode(output.id) -> output))
  }
}

object State {

  val genesisState: State = State()

  //TODO: Add support of levelDb, now always start from empty state
  def recoverState: State = genesisState
}
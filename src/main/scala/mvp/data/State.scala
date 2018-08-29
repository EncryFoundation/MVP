package mvp.data

import akka.util.ByteString
import mvp.utils.Crypto.Sha256RipeMD160
import mvp.utils.BlockchainUtils._
import scorex.util.encode.Base16

case class State(state: Map[String, Output] = Map.empty[String, Output]) {

  val stateHash: ByteString =
    Sha256RipeMD160(
      state.keys.map(key => Base16.decode(key).getOrElse(Array.emptyByteArray)).foldLeft(ByteString.empty)(_ ++ _)
    )

  def updateState(payload: Payload): State = {
    val (toAddToState, toRemoveFromState) = payload.transactions.foldLeft(Seq.empty[Output] -> Seq.empty[String]) {
      case ((toAdd, toRemove), tx) =>
        if (tx.outputs.forall(_.isInstanceOf[OutputAmount]))
          (toAdd ++ tx.outputs, toRemove ++ tx.inputs.map(input => base16Encode(input.useOutputId)))
        else (toAdd ++
          tx.outputs ++
          tx.inputs.flatMap(input => state.get(base16Encode(input.useOutputId)).map(_.closeForSpent)),
          toRemove ++ tx.inputs.map(input => base16Encode(input.useOutputId)))
    }
    State(state = (state -- toRemoveFromState) ++ toAddToState.map(output => base16Encode(output.id) -> output))
  }
}

object State {

  val genesisState: State = State()

  //TODO: Add support of levelDb, now always start from empty state
  def recoverState: State = genesisState
}
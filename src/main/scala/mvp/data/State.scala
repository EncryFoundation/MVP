package mvp.data

import akka.util.ByteString
import mvp.crypto.Sha256.Sha256RipeMD160
import mvp.utils.Base16
import mvp.utils.Base16._

case class State(state: Map[String, Output] = Map.empty[String, Output]) {

  val stateHash: ByteString =
    Sha256RipeMD160(
      state.keys.map(key => decode(key).getOrElse(ByteString.empty)).foldLeft(ByteString.empty)(_ ++ _)
    )

  def updateState(payload: Payload): State = {
    val (toAddToState, toRemoveFromState) = payload.transactions.foldLeft(Seq.empty[Output] -> Seq.empty[String]) {
      case ((toAdd, toRemove), tx) =>
        val toRemoveFromStateFromTx = tx.inputs
          .filter(input =>
            state.find(outputInState =>
              outputInState._2.id == input.useOutputId).exists(_._2.isInstanceOf[OutputAmount])
          )
        val closedForSpent: Seq[Output] = tx.inputs
          .flatMap(input => state
            .find(outputInState => outputInState._1 == Base16.encode(input.id))
            .filter(outputInfo => !outputInfo._2.isInstanceOf[OutputAmount])
            .map(_._2)
          ).map(_.closeForSpent)
        (toAdd ++ closedForSpent ++ payload.transactions.flatMap(_.outputs),
          toRemove ++ toRemoveFromStateFromTx.map(input => Base16.encode(input.useOutputId)))
    }
    State(state = (state -- toRemoveFromState) ++ toAddToState.map(output => encode(output.id) -> output))
  }
}

object State {

  val genesisOutput: OutputOpen = new OutputOpen

  val genesisState: State = State(Map(Base16.encode(genesisOutput.id) -> genesisOutput))

  //TODO: Add support of levelDb, now always start from empty state
  def recoverState: State = genesisState
}
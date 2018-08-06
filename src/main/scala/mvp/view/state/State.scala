package mvp.view.state

import mvp.modifiers.state.output.Output
import mvp.utils.Crypto.Sha256RipeMD160

case class State(state: Map[Array[Byte], Output] = Map.empty[Array[Byte], Output]) {

  val stateHash: Array[Byte] = Sha256RipeMD160(state.keys.foldLeft(Array.emptyByteArray)(_ ++ _))

  def updateState(toAdd: Seq[Output], toRemove: Seq[Array[Byte]]): State =
    State(state = (state ++ toAdd.map(output => output.id -> output)) -- toRemove)
}

object State {

  val genesisState: State = State()

  def recoverState: State = {

    //TODO: Add support of levelDb, now always start from empty state
    genesisState
  }
}

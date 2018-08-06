package mvp.view.state

import mvp.modifiers.state.output.Output

case class State(state: Map[Array[Byte], Output] = Map.empty[Array[Byte], Output]) {

  def updateState(toAdd: Seq[Output], toRemove: Seq[Array[Byte]]): State =
    State(state = (state ++ toAdd.map(output => output.id -> output)) -- toRemove)
}

object State {

  val genesisState: State = State()

  def recoverState: State = {

    //TODO: Add support of levelDb, now only empty state
    genesisState
  }
}

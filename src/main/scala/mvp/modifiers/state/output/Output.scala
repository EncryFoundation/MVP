package mvp.modifiers.state.output

import mvp.modifiers.Modifier

trait Output extends Modifier {

  def unlock(proof: Array[Byte]): Boolean
}

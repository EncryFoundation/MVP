package mvp.modifiers.state.input

import mvp.modifiers.Modifier
import mvp.utils.Crypto.Sha256RipeMD160

case class Input(useOutputId: Array[Byte],
                 proof: Array[Byte]) extends Modifier {

  override val id: Array[Byte] = Sha256RipeMD160(useOutputId ++ proof)
}

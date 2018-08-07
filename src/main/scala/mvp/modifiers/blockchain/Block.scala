package mvp.modifiers.blockchain

import mvp.modifiers.Modifier
import mvp.utils.Crypto.Sha256RipeMD160

case class Block(header: Header,
                 payload: Payload) extends Modifier {

  override val id: Array[Byte] = Sha256RipeMD160(header.id ++ payload.id)
}

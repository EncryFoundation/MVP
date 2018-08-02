package mvp.modifiers.blockchain

import mvp.modifiers.Modifier
import scorex.crypto.hash.{Blake2b256, Digest32}

case class Block(header: Header,
                 payload: Payload) extends Modifier {

  override val id: Digest32 = Blake2b256(header.id ++ payload.id)
}

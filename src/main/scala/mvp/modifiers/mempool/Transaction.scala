package mvp.modifiers.mempool

import mvp.modifiers.Modifier
import scorex.crypto.hash.{Blake2b256, Digest32}

case class Transaction(bundle: Array[Byte],
                       check: Array[Byte],
                       publicKeyHash: Array[Byte],
                       userData: Array[Byte],
                       publicKey: Array[Byte],
                       signature: Array[Byte]) extends Modifier {

  override val id: Digest32 = Blake2b256.hash(
    bundle ++ check ++ publicKeyHash ++ userData ++ publicKey ++ signature
  )
}

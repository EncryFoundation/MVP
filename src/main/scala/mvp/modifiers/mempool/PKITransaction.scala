package mvp.modifiers.mempool

import mvp.utils.Crypto.Sha256RipeMD160

case class PKITransaction(bundle: Array[Byte],
                          check: Array[Byte],
                          publicKeyHash: Array[Byte],
                          userData: Array[Byte],
                          publicKey: Array[Byte],
                          signature: Array[Byte]) extends Transaction {

  override val id: Array[Byte] = Sha256RipeMD160(
    bundle ++ check ++ publicKeyHash ++ userData ++ publicKey ++ signature
  )
}

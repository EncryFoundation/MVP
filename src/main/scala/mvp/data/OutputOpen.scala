package mvp.data

import akka.util.ByteString
import mvp.crypto.ECDSA

//nonce - magic number
class OutputOpen extends OutputAmount(ECDSA.createKeyPair.getPublic, 5000L, 123L) {

  override def unlock(proofs: Seq[ByteString]): Boolean = true
}

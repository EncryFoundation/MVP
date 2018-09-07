package mvp.local

import java.security.KeyPair
import mvp.crypto.ECDSA

object Keys {

  //Add recover from db
  def recoverKeys: Seq[KeyPair] = Seq(ECDSA.createKeyPair)
}

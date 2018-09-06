package mvp.local

import java.security.KeyPair
import mvp.crypto.ECDSA

object Keys {

  def recoverKeys: Seq[KeyPair] =
    //Add recover from db
    Keys(Seq(ECDSA.createKeyPair))
}

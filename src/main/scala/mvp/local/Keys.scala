package mvp.local

import org.encryfoundation.common.crypto.PrivateKey25519
import scorex.crypto.signatures.{Curve25519, PrivateKey, PublicKey}
import scorex.utils.Random

case class Keys(keys: Seq[PrivateKey25519])

object Keys {

  def recoverKeys: Keys = {
    //Add recover from db
    val keyPair: (PrivateKey, PublicKey) = Curve25519.createKeyPair(Random.randomBytes())
    Keys(Seq(PrivateKey25519(keyPair._1, keyPair._2)))
  }
}

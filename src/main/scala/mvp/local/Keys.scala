package mvp.local

import akka.util.ByteString
import mvp.crypto.Curve25519
import org.encryfoundation.common.crypto.PrivateKey25519
import mvp.utils.BlockchainUtils.randomByteString
import scorex.crypto.signatures.{PrivateKey, PublicKey}

case class Keys(keys: Seq[PrivateKey25519])

object Keys {

  def recoverKeys: Keys = {
    //Add recover from db
    val (privKeyBytes: ByteString, publicKeyBytes: ByteString) = Curve25519.createKeyPair(randomByteString)
    Keys(Seq(PrivateKey25519(PrivateKey @@ privKeyBytes.toArray, PublicKey @@ publicKeyBytes.toArray)))
  }
}

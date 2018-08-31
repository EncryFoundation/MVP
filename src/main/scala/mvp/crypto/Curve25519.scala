package mvp.crypto

import java.lang.reflect.Constructor
import akka.util.ByteString
import org.whispersystems.curve25519.OpportunisticCurve25519Provider
import scala.util.{Failure, Try}

object Curve25519 {

  val SignatureLength: Int = 64
  val KeyLength: Int = 32

  private val provider: OpportunisticCurve25519Provider = {
    val constructor = classOf[OpportunisticCurve25519Provider]
      .getDeclaredConstructors
      .head
      .asInstanceOf[Constructor[OpportunisticCurve25519Provider]]
    constructor.setAccessible(true)
    constructor.newInstance()
  }

  def createKeyPair(seed: ByteString): (ByteString, ByteString) = {
    val hashedSeed = Sha256.hash(seed)
    val privateKey = provider.generatePrivateKey(hashedSeed.toArray)
    ByteString(privateKey) -> ByteString(provider.generatePublicKey(privateKey))
  }

  def sign(privateKey: ByteString, message: ByteString): Try[ByteString] = Try {
    require(privateKey.length == KeyLength)
    ByteString(provider.calculateSignature(provider.getRandom(SignatureLength), privateKey.toArray, message.toArray))
  }

  def verify(signature: ByteString, message: ByteString, publicKey: ByteString): Boolean = Try {
    require(signature.length == SignatureLength)
    require(publicKey.length == KeyLength)
    provider.verifySignature(publicKey.toArray, message.toArray, signature.toArray)
  }.recoverWith { case e =>
    Failure(e)
  }.getOrElse(false)

  def createSharedSecret(privateKey: ByteString, publicKey: ByteString): ByteString = {
    ByteString(provider.calculateAgreement(privateKey.toArray, publicKey.toArray))
  }
}
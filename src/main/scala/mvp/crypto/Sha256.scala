package mvp.crypto

import java.security.{MessageDigest, Security}
import akka.util.ByteString
import org.bouncycastle.jce.provider.BouncyCastleProvider

object Sha256 {
  def Sha256RipeMD160(bytes: ByteString): ByteString = {
    Security.addProvider(new BouncyCastleProvider)
    val sha256Digest: MessageDigest = MessageDigest.getInstance("SHA-256")
    val sha256Hash: Array[Byte] = sha256Digest.digest(bytes.toArray)
    val ripeMD160Digest: MessageDigest = MessageDigest.getInstance("RipeMD160", "BC")
    ByteString(ripeMD160Digest.digest(sha256Hash))
  }

  def hash(input: ByteString): ByteString =
    ByteString(MessageDigest.getInstance("SHA-256").digest(input.toArray))
}
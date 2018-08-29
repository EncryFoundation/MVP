package mvp.utils

import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security
import java.security.MessageDigest
import akka.util.ByteString

object Crypto {

  def Sha256RipeMD160(bytes: ByteString): ByteString = {
    Security.addProvider(new BouncyCastleProvider)
    val sha256Digest: MessageDigest = MessageDigest.getInstance("SHA-256")
    val sha256Hash: Array[Byte] = sha256Digest.digest(bytes.toArray)
    val ripeMD160Digest: MessageDigest = MessageDigest.getInstance("RipeMD160", "BC")
    ByteString(ripeMD160Digest.digest(sha256Hash))
  }
}
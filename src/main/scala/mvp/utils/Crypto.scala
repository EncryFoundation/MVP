package mvp.utils

import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security
import java.security.MessageDigest

object Crypto {

  def Sha256RipeMD160(bytes: Array[Byte]): Array[Byte] = {
    Security.addProvider(new BouncyCastleProvider)
    val sha256Digest: MessageDigest = MessageDigest.getInstance("SHA-256")
    val sha256Hash: Array[Byte] = sha256Digest.digest( bytes )
    val ripeMD160Digest: MessageDigest = MessageDigest.getInstance("RipeMD160", "BC")
    ripeMD160Digest.digest(sha256Hash)
  }
}

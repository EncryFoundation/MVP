package mvp.utils

import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}
import java.security.{KeyFactory, PrivateKey, PublicKey}

import akka.util.ByteString

object ECDSAUtils {

  private val kf: KeyFactory = KeyFactory.getInstance("ECDSA", "BC")

  def str2PublicKey(str: String): PublicKey =
    kf.generatePublic(new X509EncodedKeySpec(Base16.decode(str).getOrElse(ByteString.empty).toArray))

  def str2PrivateKey(str: String): PrivateKey =
    kf.generatePrivate(new PKCS8EncodedKeySpec(Base16.decode(str).getOrElse(ByteString.empty).toArray))

  def publicKey2Str(publicKey: PublicKey): String = Base16.encode(ByteString(publicKey.getEncoded))

  def privateKey2Str(privateKey: PrivateKey): String = Base16.encode(ByteString(privateKey.getEncoded))
}

package mvp.crypto

import java.security._
import java.security.interfaces.{ECPublicKey => JSPublicKey}
import java.security.spec.{ECGenParameterSpec, ECPublicKeySpec}
import akka.util.ByteString
import org.bouncycastle.jce.interfaces.ECPublicKey
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.{ECNamedCurveParameterSpec, ECNamedCurveSpec}
import org.bouncycastle.jce.{ECNamedCurveTable, ECPointUtil}

object ECDSA {

  def createKeyPair: KeyPair = {
    val keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", "BC")
    val params = new ECGenParameterSpec("secp256k1")
    keyPairGenerator.initialize(params)
    keyPairGenerator.generateKeyPair
  }

  def sign(privateKey: PrivateKey, messageToSign: ByteString): ByteString = {
    val ecdsaSign: Signature = Signature.getInstance("SHA256withECDSA", "BC")
    ecdsaSign.initSign(privateKey)
    ecdsaSign.update(messageToSign.toArray)
    ByteString(ecdsaSign.sign)
  }

  def compressPublicKey(publicKey: PublicKey): ByteString =
    ByteString(publicKey.asInstanceOf[ECPublicKey].getQ.getEncoded(true))

  def uncompressPublicKey(compressedPublicKey: ByteString): PublicKey = {
    val spec: ECNamedCurveParameterSpec = ECNamedCurveTable.getParameterSpec("secp256k1")
    val kf: KeyFactory = KeyFactory.getInstance("ECDSA", new BouncyCastleProvider)
    val params: ECNamedCurveSpec =
      new ECNamedCurveSpec("secp256k1", spec.getCurve, spec.getG, spec.getN)
    val pubKeySpec: ECPublicKeySpec =
      new ECPublicKeySpec(ECPointUtil.decodePoint(params.getCurve, compressedPublicKey.toArray), params)
    kf.generatePublic(pubKeySpec).asInstanceOf[JSPublicKey]
  }

  def verify(signature: ByteString, message: ByteString, publicKey: ByteString): Boolean = {
    val ecdsaVerify: Signature = Signature.getInstance("SHA256withECDSA", "BC")
    ecdsaVerify.initVerify(uncompressPublicKey(publicKey))
    ecdsaVerify.update(message.toArray)
    ecdsaVerify.verify(signature.toArray)
  }
}

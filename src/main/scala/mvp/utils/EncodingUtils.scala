package mvp.utils

import java.security.{PrivateKey, PublicKey}
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}
import java.security.KeyFactory
import akka.util.ByteString
import mvp.utils.Base16.encode
import io.circe.{Decoder, Encoder}
import io.circe.syntax._

object EncodingUtils {

  implicit val byteStringEncoder: Encoder[ByteString] = bytes => encode(bytes).asJson
  implicit val byteStringDecoder: Decoder[ByteString] =
    Decoder.decodeString.map(Base16.decode).map(_.getOrElse(ByteString.empty))
  implicit val publicKeyEncoder: Encoder[PublicKey] = bytes => encode(ByteString(bytes.getEncoded)).asJson
  implicit val publicKeyDecoder: Decoder[PublicKey] =
    Decoder.decodeString.map( pkBytes =>
      kf.generatePublic(new X509EncodedKeySpec(Base16.decode(pkBytes).getOrElse(ByteString.empty).toArray))
    )
  implicit val privateKeyEncoder: Encoder[PrivateKey] = bytes => encode(ByteString(bytes.getEncoded)).asJson
  implicit val privateKeyDecoder: Decoder[PrivateKey] =
    Decoder.decodeString.map( pkBytes =>
      kf.generatePrivate(new PKCS8EncodedKeySpec(Base16.decode(pkBytes).getOrElse(ByteString.empty).toArray))
    )
}

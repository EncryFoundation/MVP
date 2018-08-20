package mvp.local.messageHolder

import io.circe.{Decoder, Encoder, HCursor}
import scorex.util.encode.Base16
import io.circe.syntax._
import mvp.local.messageTransaction.MessageInfo
import mvp.utils.Crypto.Sha256RipeMD160

case class UserMessage(message: String, metadata: Array[Byte], sender: Array[Byte], prevOutputId: Option[Array[Byte]], msgNub: Int) {

  def toMsgInfo: MessageInfo = MessageInfo(
    Sha256RipeMD160(message.getBytes),
    metadata,
    sender
  )
}

object UserMessage {

  implicit val jsonDecoder: Decoder[UserMessage] = (c: HCursor) => for {
    message <- c.downField("message").as[String]
    metadata <- c.downField("metadata").as[String]
    sender <- c.downField("sender").as[String]
    msgNum <- c.downField("msgNum").as[Int]
  } yield UserMessage(
    message,
    Base16.decode(metadata).getOrElse(Array.emptyByteArray),
    Base16.decode(sender).getOrElse(Array.emptyByteArray),
    None,
    msgNum
  )

  implicit val jsonEncoder: Encoder[UserMessage] = (b: UserMessage) => Map(
    "message" -> b.message.asJson,
    "metadata" -> Base16.encode(b.metadata).asJson,
    "sender" -> Base16.encode(b.sender).asJson,
    "msgNum" -> b.msgNub.asJson
  ).asJson
}
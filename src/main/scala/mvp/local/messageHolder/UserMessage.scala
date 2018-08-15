package mvp.local.messageHolder

import io.circe.{Decoder, Encoder, HCursor}
import scorex.util.encode.Base16
import io.circe.syntax._

case class UserMessage(message: String, sender: Array[Byte], prevOutputId: Option[Array[Byte]], msgNub: Int)

object UserMessage {

  implicit val jsonDecoder: Decoder[UserMessage] = (c: HCursor) => for {
    message <- c.downField("message").as[String]
    sender <- c.downField("sender").as[String]
    msgNum <- c.downField("msgNum").as[Int]
  } yield UserMessage(
    message,
    Base16.decode(sender).getOrElse(Array.emptyByteArray),
    None,
    msgNum
  )

  implicit val jsonEncoder: Encoder[UserMessage] = (b: UserMessage) => Map(
    "message" -> b.message.asJson,
    "sender" -> Base16.encode(b.sender).asJson,
    "msgNum" -> b.msgNub.asJson
  ).asJson
}
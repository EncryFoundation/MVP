package mvp.local.messageHolder

import io.circe.{Decoder, Encoder, HCursor}
import scorex.util.encode.Base58
import io.circe.syntax._

case class UserMessage(message: String, sender: Array[Byte], prevOutputId: Option[Array[Byte]])

object UserMessage {

  implicit val jsonDecoder: Decoder[UserMessage] = (c: HCursor) => for {
    message <- c.downField("message").as[String]
    sender <- c.downField("sender").as[String]
    prevOutputId <- c.downField("prevOutputId").as[Option[String]]
  } yield UserMessage(
    message,
    Base58.decode(sender).get,
    prevOutputId.map(str => Base58.decode(str).getOrElse(Array.emptyByteArray))
  )

  implicit val jsonEncoder: Encoder[UserMessage] = (b: UserMessage) => Map(
    "message" -> b.message.asJson,
    "sender" -> Base58.encode(b.sender).asJson,
    "prevOutputId" -> b.prevOutputId.flatMap(id => Some(Base58.encode(id))).asJson
  ).asJson
}

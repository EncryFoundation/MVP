package mvp.data

import io.circe.syntax._
import io.circe.{Decoder, Encoder, HCursor}

case class Block(header: Header, payload: Payload) extends Modifier {

  override val id: Array[Byte] = header.id
}

object Block {

  implicit val jsonDecoder: Decoder[Block] = (c: HCursor) => for {
    header <- c.downField("header").as[Header]
    payload <- c.downField("payload").as[Payload]
  } yield Block(
    header,
    payload
  )

  implicit val jsonEncoder: Encoder[Block] = (b: Block) => Map(
    "header" -> b.header.asJson,
    "payload" -> b.payload.asJson,
  ).asJson
}

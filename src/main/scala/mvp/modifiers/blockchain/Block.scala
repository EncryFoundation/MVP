package mvp.modifiers.blockchain

import io.circe.Encoder
import mvp.modifiers.Modifier
import mvp.utils.Crypto.Sha256RipeMD160
import io.circe.syntax._

case class Block(header: Header, payload: Payload) extends Modifier {

  override val id: Array[Byte] = Sha256RipeMD160(header.id ++ payload.id)
}

object Block {

  implicit val jsonEncoder: Encoder[Block] = (b: Block) => Map(
    "header" -> b.header.asJson,
    "payload" -> b.payload.asJson,
  ).asJson
}

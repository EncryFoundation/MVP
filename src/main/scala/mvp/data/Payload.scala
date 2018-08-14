package mvp.data

import io.circe.syntax._
import io.circe.{Decoder, Encoder, HCursor}
import mvp.utils.BlockchainUtils

case class Payload(transactions: Seq[Transaction]) extends Modifier {

  override val id: Array[Byte] = BlockchainUtils.merkleTree(transactions.map(_.id))
}

object Payload {

  implicit val jsonDecoder: Decoder[Payload] = (c: HCursor) => for {
    txs <- c.downField("transactions").as[Seq[Transaction]]
  } yield Payload(txs)

  implicit val jsonEncoder: Encoder[Payload] = (b: Payload) => Map(
    "transactions" -> b.transactions.map(_.asJson).asJson,
  ).asJson
}

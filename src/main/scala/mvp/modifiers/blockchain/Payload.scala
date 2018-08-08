package mvp.modifiers.blockchain

import io.circe.Encoder
import mvp.modifiers.Modifier
import mvp.modifiers.mempool.Transaction
import mvp.utils.BlockchainUtils
import io.circe.syntax._

case class Payload(transactions: Seq[Transaction]) extends Modifier {

  override val id: Array[Byte] = BlockchainUtils.merkleTree(transactions.map(_.id))
}

object Payload {

  implicit val jsonEncoder: Encoder[Payload] = (b: Payload) => Map(
    "transactions" -> b.transactions.map(_.asJson).asJson,
  ).asJson
}

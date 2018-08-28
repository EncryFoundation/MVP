package mvp.data

import mvp.utils.BlockchainUtils

case class Payload(transactions: Seq[Transaction]) extends Modifier {

  override val id: Array[Byte] = BlockchainUtils.merkleTree(transactions.map(_.id))
}
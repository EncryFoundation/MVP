package mvp.data

import akka.util.ByteString
import mvp.utils.BlockchainUtils

case class Payload(transactions: Seq[Transaction]) extends Modifier {

  override val id: ByteString = BlockchainUtils.merkleTree(transactions.map(_.id))
}
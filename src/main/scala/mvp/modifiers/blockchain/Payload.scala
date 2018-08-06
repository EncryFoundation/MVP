package mvp.modifiers.blockchain

import mvp.modifiers.Modifier
import mvp.modifiers.mempool.Transaction

case class Payload(transactions: Seq[Transaction]) extends Modifier {

  override val id: Array[Byte] = Array.emptyByteArray//MerkleTree(transactions.map(tx => LeafData @@ tx.id.untag(Digest32))).rootHash
}

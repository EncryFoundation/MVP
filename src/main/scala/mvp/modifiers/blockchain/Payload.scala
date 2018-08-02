package mvp.modifiers.blockchain

import mvp.modifiers.Modifier
import mvp.modifiers.mempool.Transaction
import scorex.crypto.authds.LeafData
import scorex.crypto.authds.merkle.MerkleTree
import scorex.crypto.hash.Digest32

case class Payload(transactions: Seq[Transaction]) extends Modifier {

  override val id: Digest32 = MerkleTree(transactions.map(tx => LeafData @@ tx.id.untag(Digest32))).rootHash
}

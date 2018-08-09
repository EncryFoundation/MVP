package mvp.utils

import mvp.utils.Crypto.Sha256RipeMD160

object BlockchainUtils {

  def merkleTree(txIds: Seq[Array[Byte]]): Array[Byte] = {
    val possibleRoot = txIds
      .sliding(2)
      .toSeq
      .map(txs => Sha256RipeMD160(txs.foldLeft(Array.emptyByteArray)(_ ++ _)))
    if (possibleRoot.size > 1) merkleTree(possibleRoot)
    else if (possibleRoot.isEmpty) Array.emptyByteArray
    else possibleRoot.head
  }
}

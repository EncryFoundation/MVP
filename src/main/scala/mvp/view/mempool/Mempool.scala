package mvp.view.mempool

import mvp.modifiers.mempool.Transaction

case class Mempool(txs: Seq[Transaction] = Seq.empty) {

  def put(txsToAdd: Seq[Transaction]): Mempool = Mempool(txs ++ txsToAdd)
}

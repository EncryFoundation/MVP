package mvp.view.blockchain.processor.transaction

import mvp.modifiers.mempool.Transaction
import mvp.view.blockchain.processor.StateContainer

trait TransactionProcessor extends StateContainer {

  def validate(transaction: Transaction): Boolean = ???

  def apply(txsToAdd: Seq[Transaction]): Unit = mempool = mempool.put(txsToAdd)
}

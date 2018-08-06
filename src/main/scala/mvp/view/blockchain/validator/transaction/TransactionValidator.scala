package mvp.view.blockchain.validator.transaction

import mvp.modifiers.mempool.Transaction
import mvp.view.blockchain.validator.StateContainer

trait TransactionValidator extends StateContainer {

  def validate(transaction: Transaction): Boolean = ???
}

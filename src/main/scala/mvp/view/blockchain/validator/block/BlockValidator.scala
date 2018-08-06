package mvp.view.blockchain.validator.block

import mvp.modifiers.blockchain.Block
import mvp.view.blockchain.validator.StateContainer

trait BlockValidator extends StateContainer {

  def validate(block: Block): Boolean = ???
}

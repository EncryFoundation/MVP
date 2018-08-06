package mvp.view.blockchain.processor.block

import mvp.modifiers.blockchain.Block
import mvp.view.blockchain.processor.StateContainer

trait BlockProcessor extends StateContainer {

  def validate(block: Block): Boolean = ???
}

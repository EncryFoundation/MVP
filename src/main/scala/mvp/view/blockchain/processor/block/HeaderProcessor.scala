package mvp.view.blockchain.processor.block

import mvp.modifiers.blockchain.Header
import mvp.view.blockchain.processor.StateContainer

trait HeaderProcessor extends StateContainer {

  def validate(header: Header): Boolean = ???

  def apply(header: Header): Unit = blockChain = blockChain.addHeader(header)
}

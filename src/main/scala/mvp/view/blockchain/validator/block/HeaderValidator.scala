package mvp.view.blockchain.validator.block

import mvp.modifiers.blockchain.Header
import mvp.view.blockchain.validator.StateContainer

trait HeaderValidator extends StateContainer {

  def validate(header: Header): Boolean = ???
}

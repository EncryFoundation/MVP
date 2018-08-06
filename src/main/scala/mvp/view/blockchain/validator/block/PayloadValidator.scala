package mvp.view.blockchain.validator.block

import mvp.modifiers.blockchain.Payload
import mvp.view.blockchain.validator.StateContainer

trait PayloadValidator extends StateContainer {

  def validate(payload: Payload): Boolean = ???
}

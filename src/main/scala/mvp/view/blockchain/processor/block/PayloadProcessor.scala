package mvp.view.blockchain.processor.block

import mvp.modifiers.blockchain.Payload
import mvp.view.blockchain.processor.StateContainer

trait PayloadProcessor extends StateContainer {

  def validate(payload: Payload): Boolean = ???

  def apply(payload: Payload): Unit = blockChain = blockChain.addPayload(payload)
}

package mvp.view.blockchain.processor

import mvp.modifiers.Modifier
import mvp.modifiers.blockchain.{Header, Payload}
import mvp.modifiers.mempool.Transaction

trait ModifiersProcessor extends StateContainer {

  def apply(modifier: Modifier): Unit = modifier match {
    case header: Header => blockChain = blockChain.addHeader(header)
    case payload: Payload => blockChain = blockChain.addPayload(payload)
    case transaction: Transaction => mempool = mempool.put(Seq(transaction))
  }

  def validate(modifier: Modifier): Boolean = modifier match {
    case header: Header => ???
    case payload: Payload => ???
    case transaction: Transaction => ???
  }
}

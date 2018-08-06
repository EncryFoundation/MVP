package mvp.view.blockchain.validator

import mvp.view.blockchain.Blockchain
import mvp.view.state.State

trait StateContainer {

  var blockChain: Blockchain

  var state: State
}

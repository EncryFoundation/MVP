package mvp.view.blockchain.processor

import mvp.view.blockchain.Blockchain
import mvp.view.mempool.Mempool
import mvp.view.state.State

trait StateContainer {

  var blockChain: Blockchain

  var state: State

  var mempool: Mempool
}

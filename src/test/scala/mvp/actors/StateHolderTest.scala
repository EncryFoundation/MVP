package mvp.actors

import mvp.data._
import org.scalatest.{Matchers, PropSpec}
import mvp.utils.BlockchainUtils.randomByteString
import mvp.utils.Base16.encode
import utils.TestGenerator._

class StateHolderTest extends PropSpec with Matchers {

  property("Dummy blockchain impl") {

    val initialOutputsQty: Int = 9
    val blockchainHeight: Int = 9

    val initialOutput: Seq[Output] =
      generateDummyAmountOutputs(initialOutputsQty)
    val inputs: Seq[Input] = initialOutput.map(output => Input(output.id, Seq(randomByteString)))
    val dummyBlockchain: Seq[Block] = generateBlockChainWithAmountPayloads(blockchainHeight + 1, inputs)

    val blockchain: Blockchain = Blockchain.recoverBlockchain
    val state: State = State(initialOutput.map(output => encode(output.id) -> output).toMap)

    val newState: State = dummyBlockchain.foldLeft(state) {
      case (stateDuringApply, block) => stateDuringApply.updateState(block.payload)
    }

    val newBlockchain: Blockchain = dummyBlockchain.foldLeft(blockchain) {
      case (blockchainDuringApply, block) => blockchainDuringApply.addHeader(block.header).addPayload(block.payload)
    }

    assert(!(newState.stateHash == state.stateHash), "State hash should not be equal")
    assert(newState.state.size == state.state.size, "not all boxes were spent")
    assert(newBlockchain.headersHeight == blockchainHeight, "Incorrect headers height")
    assert(newBlockchain.blockchainHeight == blockchainHeight, "Incorrect blockchain height")
  }
}

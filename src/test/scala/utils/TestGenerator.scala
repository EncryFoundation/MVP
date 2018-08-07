package utils

import mvp.modifiers.blockchain.{Block, Header, Payload}
import mvp.modifiers.mempool.Transaction
import mvp.modifiers.state.input.Input
import mvp.modifiers.state.output.{AmountOutput, Output}
import scorex.utils.Random

object TestGenerator {

  def generateHeaderChain(qty: Int): Seq[Header] = (0 to qty).foldLeft(Seq.empty[Header]) {
    case (headers, height) =>
      val lastHeaderId: Array[Byte] = if (headers.nonEmpty) headers.last.id else Array.emptyByteArray
      headers :+ Header(0L, height, lastHeaderId, Random.randomBytes(), Random.randomBytes())
  }

  def generateBlockChainWithAmountPayloads(qty: Int, initialInputs: Seq[Input]): Seq[Block] =
    generateHeaderChain(qty).foldLeft(Seq.empty[Block]){
      case (blocks, header) =>
        if (blocks.isEmpty) blocks :+ Block(header, Payload(generatePaymentTxs(initialInputs)))
        else blocks :+ Block(header, Payload(
          generatePaymentTxs(blocks.last.payload.transactions.flatMap(_.outputs.map(output => Input(output.id, Random.randomBytes())))))
        )
    }

  def generateDummyAmountOutputs(qty: Int): Seq[Output] = (0 to qty).map(i => AmountOutput(Random.randomBytes(), 100L))

  def generatePaymentTxs(inputs: Seq[Input]): Seq[Transaction] = inputs.foldLeft(Seq.empty[Transaction]) {
    case (transatcions, input) =>
      transatcions :+ Transaction(0L, Seq(input), Seq(AmountOutput(Random.randomBytes(), 100)))
  }
}

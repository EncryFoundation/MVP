package utils

import java.security.Security
import akka.util.ByteString
import mvp.crypto.ECDSA
import mvp.data.{OutputAmount, _}
import mvp.utils.BlockchainUtils.randomByteString
import org.bouncycastle.jce.provider.BouncyCastleProvider
import scala.util.Random

object TestGenerator {

  Security.addProvider(new BouncyCastleProvider)

  def generateHeaderChain(qty: Int): Seq[Header] = (0 until qty).foldLeft(Seq.empty[Header]) {
    case (headers, height) =>
      val lastHeaderId: ByteString = if (headers.nonEmpty) headers.last.id else ByteString.empty
      headers :+ Header(0L, height, lastHeaderId, randomByteString, randomByteString)
  }

  def generateBlockChainWithAmountPayloads(blocksQty: Int, initialInputs: Seq[Input]): Seq[Block] =
    generateHeaderChain(blocksQty).foldLeft(Seq.empty[Block]){
      case (blocks, header) =>
        if (blocks.isEmpty) {
          val payload: Payload = Payload(generatePaymentTxs(initialInputs))
          blocks :+ Block(header.copy(merkleTreeRoot = payload.id), payload)
        }
        else {
          val payload: Payload = Payload(
            generatePaymentTxs(
              blocks.last
                .payload
                .transactions
                .flatMap(_.outputs.map(output => Input(output.id, Seq(randomByteString)))))
          )
          blocks :+ Block(header.copy(merkleTreeRoot = payload.id), payload)
        }
    }

  def generateDummyAmountOutputs(qty: Int): Seq[Output] =
    (0 until qty).map(i => OutputAmount(ECDSA.createKeyPair.getPublic, 100L, Random.nextLong()))


  def generatePaymentTxs(inputs: Seq[Input]): Seq[Transaction] = inputs.foldLeft(Seq.empty[Transaction]) {
    case (transatcions, input) =>
      transatcions :+ Transaction(0L, Seq(input),
        Seq(OutputAmount(ECDSA.createKeyPair.getPublic, 100L, Random.nextLong())))
  }
}
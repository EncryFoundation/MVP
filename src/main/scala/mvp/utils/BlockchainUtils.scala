package mvp.utils

import akka.util.ByteString
import mvp.crypto.Sha256.Sha256RipeMD160
import scala.collection.mutable.ArrayBuffer

object BlockchainUtils {

  def merkleTree(txIds: Seq[ByteString]): ByteString = {
    val possibleRoot = txIds
      .sliding(2)
      .toSeq
      .map(txs => Sha256RipeMD160(txs.foldLeft(ByteString.empty)(_ ++ _)))
    if (possibleRoot.size > 1) merkleTree(possibleRoot)
    else if (possibleRoot.isEmpty) ByteString.empty
    else possibleRoot.head
  }

  def randomByteString: ByteString = ByteString(Random.randomBytes())

  def toByteString(value: Long): ByteString = {
    val result = ArrayBuffer[Byte]()
    var localValue = value
    (7 to 0 by -1).foreach { _ =>
      result.append((localValue & 0xffL).toByte)
      localValue >>= 8
    }
    ByteString(result.toArray)
  }

  def toByteString(value: Int): ByteString = {
    ByteString(
      Array(value >> 24, value >> 16, value >> 8, value).map(_.toByte)
    )
  }
}
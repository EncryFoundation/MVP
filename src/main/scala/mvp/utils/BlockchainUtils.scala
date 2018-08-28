package mvp.utils

import akka.util.ByteString
import mvp.utils.Crypto.Sha256RipeMD160
import scorex.util.encode.Base16
import scorex.utils.Random
import scala.collection.mutable.ArrayBuffer
import scala.util.Try

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

  def base16Encode(bytes: ByteString): String = Base16.encode(bytes.toArray)
  def base16Decode(str: String): Try[ByteString] = Base16.decode(str).map(ByteString(_))

  def toByteArray(value: Long): ByteString = {
    val result = ArrayBuffer[Byte]()
    var localValue = value
    (7 to 0 by -1).foreach { _ =>
      result.append((localValue & 0xffL).toByte)
      localValue >>= 8
    }
    ByteString(result.toArray)
  }

  def toByteArray(value: Int): ByteString = {
    ByteString(
      Array(value >> 24, value >> 16, value >> 8, value).map(_.toByte)
    )
  }
}
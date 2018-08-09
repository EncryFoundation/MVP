package mvp.modifiers.blockchain

import com.google.common.primitives.{Bytes, Ints, Longs}
import mvp.modifiers.Modifier
import mvp.utils.Crypto.Sha256RipeMD160

case class Header(timestamp: Long,
                  height: Int,
                  previousBlockHash: Array[Byte],
                  minerSignature: Array[Byte],
                  merkleTreeRoot: Array[Byte]) extends Modifier {

  val messageToSign: Array[Byte] = Bytes.concat(
    Longs.toByteArray(timestamp) ++ Ints.toByteArray(height) ++ previousBlockHash ++ merkleTreeRoot
  )

  override val id: Array[Byte] = Sha256RipeMD160(
    Longs.toByteArray(timestamp) ++ Ints.toByteArray(height) ++ previousBlockHash ++ minerSignature ++ merkleTreeRoot
  )
}

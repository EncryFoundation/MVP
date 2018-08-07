package mvp.modifiers.blockchain

import com.google.common.primitives.{Ints, Longs}
import mvp.modifiers.Modifier
import mvp.utils.Crypto.Sha256RipeMD160

case class Header(timestamp: Long,
                  height: Int,
                  previousBlockHash: Array[Byte],
                  minerSignature: Array[Byte],
                  merkleTreeRoot: Array[Byte]) extends Modifier {

  override val id: Array[Byte] = Sha256RipeMD160(
    Longs.toByteArray(timestamp) ++ Ints.toByteArray(height) ++ previousBlockHash ++ minerSignature ++ merkleTreeRoot
  )
}

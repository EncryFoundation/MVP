package mvp.modifiers.blockchain

import com.google.common.primitives.Longs
import mvp.modifiers.Modifier
import scorex.crypto.hash.{Blake2b256, Digest32}

case class Header(timestamp: Long,
                  previousBlockHash: Array[Byte],
                  minerSignature: Array[Byte],
                  merkleTreeRoot: Array[Byte]) extends Modifier {

  override val id: Digest32 = Blake2b256(Longs.toByteArray(timestamp) ++ previousBlockHash ++ minerSignature ++ merkleTreeRoot)
}

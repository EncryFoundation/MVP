package mvp.data

import akka.util.ByteString
import mvp.crypto.Sha256.Sha256RipeMD160
import mvp.utils.EncodingUtils

case class Input(useOutputId: ByteString,
                 proofs: Seq[ByteString]) extends Modifier {

  override val id: ByteString = Sha256RipeMD160(useOutputId ++ proofs.flatten)
}
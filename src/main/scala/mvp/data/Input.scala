package mvp.data

import akka.util.ByteString
import mvp.utils.Crypto.Sha256RipeMD160

case class Input(useOutputId: ByteString,
                 proofs: Seq[ByteString]) extends Modifier {

  override val id: ByteString = Sha256RipeMD160(useOutputId ++ proofs.flatten)
}
package mvp.data

import com.google.common.primitives.Longs
import io.circe.syntax._
import io.circe.{Decoder, Encoder, HCursor}
import mvp.utils.Crypto.Sha256RipeMD160

case class Transaction(timestamp: Long,
                       inputs: Seq[Input],
                       outputs: Seq[Output]) extends Modifier {

  override val id: Array[Byte] = Sha256RipeMD160(
    Longs.toByteArray(timestamp) ++
      inputs.foldLeft(Array.emptyByteArray)(_ ++ _.id) ++
      outputs.foldLeft(Array.emptyByteArray)(_ ++ _.id)
  )
}

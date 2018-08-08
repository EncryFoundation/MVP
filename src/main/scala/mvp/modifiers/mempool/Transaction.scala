package mvp.modifiers.mempool

import com.google.common.primitives.Longs
import io.circe.Encoder
import mvp.modifiers.Modifier
import mvp.modifiers.state.input.Input
import mvp.modifiers.state.output.Output
import mvp.utils.Crypto.Sha256RipeMD160
import io.circe.syntax._

case class Transaction(timestamp: Long,
                       inputs: Seq[Input],
                       outputs: Seq[Output]) extends Modifier {

  override val id: Array[Byte] = Sha256RipeMD160(
    Longs.toByteArray(timestamp) ++
      inputs.foldLeft(Array.emptyByteArray)(_ ++ _.id) ++
      outputs.foldLeft(Array.emptyByteArray)(_ ++ _.id)
  )
}

object Transaction {

  implicit val jsonEncoder: Encoder[Transaction] = (b: Transaction) => Map(
    "timestamp" -> b.timestamp.asJson,
    "inputs" -> b.inputs.map(_.asJson).asJson,
    "outputs" -> b.outputs.map(_.asJson).asJson
  ).asJson

  type Address = Array[Byte]
}

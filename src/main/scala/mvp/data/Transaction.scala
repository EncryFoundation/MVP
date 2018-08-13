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

object Transaction {

  implicit val jsonDecoder: Decoder[Transaction] = (c: HCursor) => for {
    timestamp <- c.downField("timestamp").as[Long]
    inputs <- c.downField("inputs").as[Seq[Input]]
    outputs <- c.downField("outputs").as[Seq[Output]]
  } yield Transaction(
    timestamp,
    inputs,
    outputs
  )

  implicit val jsonEncoder: Encoder[Transaction] = (b: Transaction) => Map(
    "timestamp" -> b.timestamp.asJson,
    "inputs" -> b.inputs.map(_.asJson).asJson,
    "outputs" -> b.outputs.map(_.asJson).asJson
  ).asJson
}
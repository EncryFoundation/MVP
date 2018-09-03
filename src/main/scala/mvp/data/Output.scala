package mvp.data

import akka.util.ByteString
import com.typesafe.scalalogging.StrictLogging
import io.circe.{Decoder, Encoder}
import io.circe.syntax._
import io.circe.generic.auto._
import cats.syntax.functor._
import mvp.utils.BlockchainUtils._
import mvp.utils.Base16._
import mvp.utils.EncodingUtils._
import mvp.crypto.Curve25519

trait Output extends Modifier with StrictLogging {

  val messageToSign: ByteString

  val publicKey: ByteString

  val signature: ByteString

  val canBeSpent: Boolean

  def checkSignature: Boolean = {
    val result: Boolean = Curve25519.verify(signature, messageToSign, publicKey)
    logger.info(s"Going to check signature for output with id: ${encode(id)} and result is: $result")
    result
  }

  def closeForSpent: Output

  def unlock(proofs: Seq[ByteString]): Boolean
}

object Output {

  implicit val jsonDencoder: Decoder[Output] =
    List[Decoder[Output]](
      Decoder[OutputAmount].widen,
      Decoder[OutputPKI].widen,
      Decoder[OutputMessage].widen
    ).reduceLeft(_ or _)

  implicit val jsonEncoder: Encoder[Output] = {
    case ab: OutputAmount => ab.asJson
    case db: OutputPKI => db.asJson
    case aib: OutputMessage => aib.asJson
  }
}
package mvp.data

import java.security.PublicKey
import akka.util.ByteString
import cats.syntax.functor._
import com.typesafe.scalalogging.StrictLogging
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import mvp.utils.EncodingUtils._

trait Output extends Modifier with StrictLogging {

  val nonce: Long

  val messageToSign: ByteString

  val publicKey: PublicKey

  val canBeSpent: Boolean

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
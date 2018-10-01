package mvp.data

import akka.util.ByteString

trait PublicKeyContainable {

  val publicKey: ByteString
}

package mvp.data

import akka.util.ByteString

trait AddressContainable {

  val address: ByteString
}

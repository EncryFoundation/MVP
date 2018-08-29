package mvp.data

import akka.util.ByteString

case class Block(header: Header, payload: Payload) extends Modifier {

  override val id: ByteString = header.id
}
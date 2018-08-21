package mvp.data

case class Block(header: Header, payload: Payload) extends Modifier {

  override val id: Array[Byte] = header.id
}
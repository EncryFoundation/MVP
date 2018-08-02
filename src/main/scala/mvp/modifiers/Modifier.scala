package mvp.modifiers

import scorex.crypto.hash.Digest32

trait Modifier {

  val id: Digest32
}

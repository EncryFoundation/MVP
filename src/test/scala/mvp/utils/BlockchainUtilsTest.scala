package mvp.utils

import org.scalatest.{Matchers, PropSpec}
import mvp.utils.BlockchainUtils.randomByteString

class BlockchainUtilsTest extends PropSpec with Matchers{

  property("merkleTree property") {

    BlockchainUtils.merkleTree((0 to 9).map(_ => randomByteString).toList)
  }

}

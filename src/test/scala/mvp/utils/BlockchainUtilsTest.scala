package mvp.utils

import org.scalatest.{Matchers, PropSpec}
import scorex.utils.Random

class BlockchainUtilsTest extends PropSpec with Matchers{

  property("merkleTree property") {

    BlockchainUtils.merkleTree((0 to 9).map(i => Random.randomBytes()).toList)
  }

}

package nyhx.sequence

import models.{ClientRequest, Commands, Result}
import org.scalatest._
import sources.ImageSources
import Find.findPicBuilding2FindAux

class FindTest extends WordSpec with Matchers with ImageSources {
  "returns" in {
    val result = Find.returns(ClientRequest(readOriginal("adventure.png"))).run()
    assert(result.isFind)
  }
  "touch" must {
    "is find" in {

      val result = Find.returns.touch(ClientRequest(readOriginal("adventure.png")))
      result shouldBe a[Result.Success]
    }
    "no find" in {

      val result = Find.returns.touch(ClientRequest(readOriginal("room.png")))

      result shouldBe a[Result.Failure]
    }
  }
}

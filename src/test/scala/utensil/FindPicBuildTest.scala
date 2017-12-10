package utensil

import nyhx.Images
import org.scalatest._
import sources.ImageSources

class FindPicBuildTest extends WordSpec with Matchers with ImageSources {
  val original = readOriginal("room.png")
  "wdj" must {
    "wdj" in {
      val goal = Images.Wdj.wuDouJi.toGoal
      val result = FindPicBuild()
        .withGoal(goal)
        .withOriginal(original)
        .run()
      assert(result.isFind)
    }
    "patten" in {
      val goal = Images.Wdj.wuDouJi.toGoal
      val result = FindPicBuild()
        .withGoal(goal)
        .withOriginal(original)
        .run()
      assert(result.isFind)
    }
    "sen shen" in {
      val goal = Images.Wdj.shenShen.toGoal
      val result = FindPicBuild()
        .withGoal(goal)
        .withOriginal(original)
        .run()
      assert(result.noFind)
    }
  }
}

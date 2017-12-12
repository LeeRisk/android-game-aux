package nyhx

import models.{ClientRequest, GoalImage, Image}
import utensil.{FindPicBuild, FindPicResult}

object Find {
  def find(image: GoalImage) = (clientRequest: ClientRequest) => FindPicBuild()
    .withGoal(image.toGoal)
    .withOriginal(clientRequest.image.toOriginal)

  def apply(image: Image) = find(image.toGoal)


  implicit class FindPicBuildingWithRun(f: ClientRequest => FindPicBuild[FindPicBuild.Request]) {
    def run(c: ClientRequest): FindPicResult = f(c).run()
  }
}

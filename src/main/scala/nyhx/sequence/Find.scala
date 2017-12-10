package nyhx.sequence

import scala.language.implicitConversions

import models._
import nyhx.Images
import org.slf4j.LoggerFactory
import utensil.{FindPicBuild, IsFindPic, NoFindPic}

object Find {
  val returns           = find(Images.returns.toGoal)
  val goToRoom          = find(Images.returns_room.toGoal)
  val goToGakuen        = find(Images.returns_gakuen.toGoal)
  val adventure         = find(Images.Adventure.adventure.toGoal)
  val grouping          = find(Images.Adventure.grouping.toGoal)
  val start             = find(Images.start.toGoal)
  val totalTurn         = find(Images.Adventure.totalTurn.toGoal)
  val mpEmpty           = find(Images.Adventure.mpEmpty.toGoal)
  val navigateCondition = find(Images.Adventure.navigateCondition.toGoal)
  val determine         = find(Images.determine.toGoal)

  def find(image: GoalImage) = (clientRequest: ClientRequest) => FindPicBuild()
    .withGoal(image.toGoal)
    .withOriginal(clientRequest.image.toOriginal)

  def apply(image: GoalImage) = find(image)

  implicit def findPicBuilding2FindAux[X <: FindPicBuild.Request](f: ClientRequest => FindPicBuild[X]): FindAux = new FindAux(f)
}

class FindAux(f: ClientRequest => FindPicBuild[FindPicBuild.Request]) {
  val logger = LoggerFactory.getLogger("find-aux")

  //常用的模式之一 if find then touch else ???
  def touch = RecAction { implicit c =>
    val findPicBuild = f(c)
    val result = findPicBuild.run()
    val name = findPicBuild.goal.get.simpleName
    logger.info(s"find $name : (${result.isFind})")
    result match {
      case IsFindPic(point) => Result.Success(Commands().addTap(point))
      case NoFindPic()      => Result.Failure(NoFindPicException(name))
    }
  }

  //常用的模式之一 if no find then continue else goto next
  def waitFind = RecAction { implicit c =>
    val findPicBuild = f(c)
    val result = findPicBuild.run()
    val name = findPicBuild.goal.get.simpleName
    logger.info(s"wait find $name : (${result.isFind})")
    result match {
      case IsFindPic(point) => Result.Success()
      case NoFindPic()      => Result.Execution(Commands())
    }
  }

}

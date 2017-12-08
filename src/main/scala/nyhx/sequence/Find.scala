package nyhx.sequence

import scala.language.implicitConversions

import models.{ClientRequest, Commands, GoalImage}
import nyhx.{Images, NoFindPicException, RecAction, Result}
import org.slf4j.LoggerFactory
import utensil.{FindPicBuild, IsFindPic, NoFindPic}

object Find {
  val returns           = find(Images.returns.toGoal)
  val goToRoom          = find(Images.returns_room.toGoal)
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


  implicit def findPicBuilding2FindAux[X <: FindPicBuild.Request](f: ClientRequest => FindPicBuild[X]): FindAux = new FindAux(f)
}

class FindAux(f: ClientRequest => FindPicBuild[FindPicBuild.Request]) {
  val logger = LoggerFactory.getLogger("find-aux")

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

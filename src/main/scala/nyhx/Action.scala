package nyhx

import models.{GoalImage, Point}
import org.slf4j.LoggerFactory
import utensil.FindPic

case class FindPicAction(name: String,
                         isFind: Option[Point => Result] = None,
                         noFind: Option[() => Result] = None,
                         goalImage: Option[GoalImage] = None) {
  val logger = LoggerFactory.getLogger("find-pic")

  def withIsFind(f: Point => Result) = this.copy(isFind = Some(f))

  def withNoFind(f: () => Result) = this.copy(noFind = Some(f))

  def withGoal(goalImage: GoalImage) = this.copy(goalImage = Some(goalImage))

  def run() = {
    assert(isFind.nonEmpty)
    assert(noFind.nonEmpty)
    assert(goalImage.nonEmpty)

    RecAction { implicit c =>
      logger.info(s" do {{$name}}")
      val goal = goalImage.get
      val result = FindPic(c.image.toOriginal, goal)
      result.point match {
        case Some(point) =>
          logger.info(s"${goal.simpleName} is find")
          isFind.map(f => f(point)).get
        case None        =>
          logger.info(s"${goal.simpleName} no find")
          noFind.map(f => f()).get
      }

    }
  }
}

object HelpFunc {

}
package nyhx.back

import models.{Commands, GoalImage, Point}
import nyhx.{Images, RecAction, Result}
import org.slf4j.LoggerFactory
import utensil.FindPic

//TODO remove it
case class FindPicAction(name: String,
                         isFind: Option[Point => Result] = None,
                         noFind: Option[() => Result] = None,
                         goalImage: Option[GoalImage] = None,
                         patten: Option[FindPic.Patten.Value] = None) {
  val logger = LoggerFactory.getLogger("find-pic")

  def withIsFind(f: Point => Result) = this.copy(isFind = Some(f))

  def withNoFind(f: () => Result) = this.copy(noFind = Some(f))

  def withGoal(goalImage: GoalImage) = this.copy(goalImage = Some(goalImage))

  def withPatten(patten: FindPic.Patten.Value) = this.copy(patten = Some(patten))

  def run() = {
    assert(isFind.nonEmpty)
    assert(noFind.nonEmpty)
    assert(goalImage.nonEmpty)

    RecAction { implicit c =>
      logger.info(s" do {{$name}}")
      val goal = goalImage.get
      val result = FindPic(c.image.toOriginal, goal, patten = patten.getOrElse(FindPic.Patten.Default))
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

object Actions {
  def touchReturns: RecAction =
    FindPicAction("touch return")
      .withIsFind(e => Result.Execution(Commands().addTap(e).addDelay(1000)))
      .withNoFind(() => Result.Success(Commands()))
      .withGoal(Images.returns.toGoal)
      .run()

  def goToRoom: RecAction =
    FindPicAction("go to room")
      .withIsFind(e => Result.Execution(Commands().addTap(e).addDelay(1000)))
      .withNoFind(() => Result.Success(Commands()))
      .withGoal(Images.returns_room.toGoal)
      .run()


  def utilFind(maxNum: Int = 100, image: GoalImage)(implicit patten: FindPic.Patten.Value = FindPic.Patten.Default): RecAction =
    FindPicAction(s"util find ${image.simpleName}")
      .withIsFind(e => Result.Success())
      .withNoFind(() => Result.Become(utilFind(maxNum - 1, image)))
      .withGoal(image)
      .run()

  def utilFindAndTouch(maxNum: Int = 100, image: GoalImage)(implicit patten: FindPic.Patten.Value = FindPic.Patten.Default): RecAction =
    FindPicAction(s"util find and touch ${image.simpleName}")
      .withIsFind(e => Result.Success(Commands().addTap(e)))
      .withNoFind(() => Result.Become(utilFind(maxNum - 1, image)))
      .withGoal(image)
      .run()

  def findAndTouch(goalImage: GoalImage)(implicit patten: FindPic.Patten.Value = FindPic.Patten.Default): RecAction =
    FindPicAction("find and touch")
      .withIsFind(e => Result.Success(Commands().addTap(e)))
      .withNoFind(() => Result.Failure(new Exception(s"no find ${goalImage.simpleName}")))
      .withGoal(goalImage)
      .run()

  def touchUtilNoFind( image: GoalImage,f: Point =>Point) =
    FindPicAction("find and touch")
      .withIsFind(e => Result.Execution(Commands().addTap(f(e))))
      .withNoFind(() => Result.Success())
      .withGoal(image)
      .run()

}
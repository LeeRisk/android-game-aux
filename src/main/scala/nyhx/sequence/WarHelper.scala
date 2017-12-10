package nyhx.sequence

import models._
import nyhx.{Images, Points}
import utensil.{IsFindPic, NoFindPic}
import Find.findPicBuilding2FindAux
import akka.actor.Actor

trait WarHelper {
  this: ScenesHelper with BaseHelper with Actor =>
  /**
    * tap result
    * go to room
    * find adventure in room
    * tap adventure
    * -- end
    */
  def goToAdventure = (Sequence("goToAdventure")
    next touchReturns
    next goToRoom
    next mustFind(Find.adventure(_))
    next Find.adventure.touch
    util(Find.grouping.waitFind, 10)
    )

  // tap grouping
  // check mp
  // tap start
  def warReady = (Sequence("warReady")
    next Find.grouping.touch
    next checkMpEmpty
    next Find.start.touch
    next checkWarIsStart
    )

  def checkWarIsStart = RecAction { implicit c =>
    Find.start(c).run() match {
      case IsFindPic(point) =>
        println("start war failure")
        context.parent ! WarTaskEnd(self)
        Result.Failure(WarStartFailure())
      case NoFindPic()      =>
        Result.Success()
    }
  }


  // tap point
  // tap start
  // wait war end
  // sure reward
  def warPoint(point: Point) = (Sequence("warPoint")
    next Find.navigateCondition.waitFind
    next justTap(point, 2000)
    next Find.start.waitFind
    next Find.start.touch
    next waitWarEnd
    next sureWarReward
    )

  def randomPoint(point: Point) = (Sequence(s"random point :${point.name}")
    next Find.navigateCondition.waitFind
    next justTap(point, 2000)
    next Find(Images.Adventure.selectA.toGoal).waitFind
    next Find(Images.Adventure.selectA.toGoal).touch
    next justTap(Point(1, 1), 500)
    next justTap(Point(1, 1), 500)
    )

  def warEnd = (Sequence("warEnd")
    next Find.returns.waitFind
    next Find.returns.touch
    next Find.determine.waitFind
    next Find.determine.touch
    util(Find.grouping.waitFind, 10)
    )

  def waitWarEnd = Find.totalTurn.waitFind

  def goToWarArea(area: Point, zone: Int) = RecAction { implicit c =>
    val toArea = Commands()
      .addTap(Points.Area.one).addDelay(1000)
      .addTap(area).addDelay(1000)
    Result.Success(
      (1 until zone).foldLeft(toArea)((l, r) => l.addTap(Points.Adventure.next).addDelay(1000))
    )
  }

  def checkMpEmpty = RecAction { implicit c =>
    val result = Find.mpEmpty(c).run()
    result match {
      case IsFindPic(point) =>
        logger.warn("mp empty in war;")
        Result.Failure(EmEmptyException())
      case NoFindPic()      =>
        logger.warn("check mp success;")
        Result.Success(Commands())
    }
  }

  def sureWarReward = RecAction { implicit c =>
    val result = Find.navigateCondition(c).run()

    result match {
      case IsFindPic(point) =>
        logger.info("get war reward ; go to next")
        Result.Success()

      case NoFindPic() =>
        logger.info("have not get war reward ; try again")
        Result.Execution(Commands().addTap(Point(0, 0)))
    }
  }

}

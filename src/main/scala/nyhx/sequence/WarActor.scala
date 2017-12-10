package nyhx.sequence

import scala.language.implicitConversions

import akka.actor.Actor
import models.{ClientRequest, Commands, Point, _}
import nyhx._
import nyhx.sequence.Find.findPicBuilding2FindAux
import org.slf4j.LoggerFactory
import utensil.{FindPicBuild, IsFindPic, NoFindPic}

class WarActor extends Actor with Scenes {
  def rec(action: Sequence): PartialFunction[Any, Sequence] = PartialFunction { case c: ClientRequest =>
    Sequence.run(action)(c, sender())
  }

  def onRec(action: Sequence): Receive =
    rec(action).andThen(action => context.become(onRec(action)))

  override def receive: Receive = onRec(sequences)

  val sequences: Sequence = (Sequence("war")
    next goToAdventure
    next goToWarArea(Points.Area.six, 4)
    repeat(warPoint_B, 100)
    )


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

  def warPoint_B = {
    // goto adventure
    // war point(B)
    // exit war
    (Sequence("warPoint_B")
      next warReady
      next warPoint(Points.Adventure.AreaSix.b)
      next warEnd
      )
  }


  // tap grouping
  // check mp
  // tap start
  def warReady = (Sequence("warReady")
    next Find.grouping.touch
    next checkMpEmpty
    next Find.start.touch
    )

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

  def warEnd = (Sequence("warEnd")
    next Find.returns.waitFind
    next Find.returns.touch
    next Find.determine.waitFind
    next Find.determine.touch
    util(Find.grouping.waitFind, 10)
    )


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

  val logger = LoggerFactory.getLogger("war")

  def justTap(point: Point, delay: Int) =
    RecAction(e => Result.Success(Commands().addTap(point).addDelay(delay)))

  def justDelay(delay: Int) =
    RecAction(e => Result.Success(Commands().addDelay(delay)))

  def mustFind[T <: FindPicBuild.Request](f: ClientRequest => FindPicBuild[T]) = RecAction { implicit clientRequest =>
    val findPicBuild = f(clientRequest)
    findPicBuild.run() match {
      case IsFindPic(_) => Result.Success()
      case NoFindPic()  => Result.Failure(NoFindPicException(findPicBuild.goal.get.simpleName))
    }
  }


  def end = RecAction { implicit c => println("end"); ??? }


  //  {
  //    case c: ClientRequest =>
  //      val result = Sequence.run(action)(c, sender())
  //      action = result
  //  }
}

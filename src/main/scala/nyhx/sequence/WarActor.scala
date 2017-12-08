package nyhx.sequence

import scala.language.implicitConversions

import akka.actor.{Actor, ActorRef}
import models.{ClientRequest, Commands, Point}
import nyhx._
import org.slf4j.LoggerFactory
import utensil.{FindPicBuild, IsFindPic, NoFindPic}


class WarActor extends Actor with Scenes {
  var action = warPoint_B


  /**
    * tap result
    * go to room
    * find adventure in room
    * tap adventure
    * -- end
    */
  def goToAdventure = (Sequence()
    next touchReturns
    next goToRoom
    next mustFind(Find.adventure(_))
    next touchAdventure
    util(waitFindGrouping, 10)
    )

  def warPoint_B = {
    // goto adventure
    // war point(B)
    // exit war
    (Sequence()
      next warReady
      next warPoint(Points.Adventure.AreaSix.b)
      next warEnd
      )
  }


  // tap grouping
  // check mp
  // tap start
  def warReady = (Sequence()
    next touchGrouping
    next checkMpEmpty
    next touchStart
    )

  // tap point
  // tap start
  // wait war end
  // sure reward
  def warPoint(point: Point) = (Sequence()
    next justTap(point, 2000)
    next touchStart
    next waitWarEnd
    next sureWarReward
    )

  def warEnd = (Sequence()
    next touchReturns
    next touchDetermine
    util(waitFindGrouping, 10)

    )


  def sureWarReward = RecAction { implicit c =>
    val result = Find.navigateCondition.run()

    result match {
      case IsFindPic(point) =>
        logger.info("get war reward ; go to next")
        Result.Success()

      case NoFindPic() =>
        logger.info("have not get war reward ; try again")
        Result.Execution(Commands().addTap(Point(0, 0)))
    }
  }

  def waitWarEnd = RecAction { implicit c =>
    val result = Find.totalTurn.run()
    logger.info(s"war is end : ${result.isFind}")
    result match {
      case IsFindPic(point) => Result.Success()
      case NoFindPic()      => Result.Execution(Commands())
    }
  }


  def goToWarArea(area: Point, zone: Int) = RecAction { implicit c =>
    val toArea = Commands()
      .addTap(Points.Area.one).addDelay(1000)
      .addTap(area).addDelay(1000)
    Result.Success(
      (1 until zone).foldLeft(toArea)((l, r) => l.addTap(Points.Adventure.next).addDelay(1000))
    )
  }

  def touchGrouping = RecAction { implicit c =>
    val result = Find.grouping.run()
    logger.info(s"find grouping : (${result.isFind})")
    result match {
      case IsFindPic(point) => Result.Success(Commands().addTap(point))
      case NoFindPic()      => Result.Failure(NoFindPicException("grouping"))
    }
  }


  def waitFindGrouping = RecAction { implicit c =>
    val result = Find.grouping.run()
    logger.info(s"wait find grouping : (${result.isFind})")
    result match {
      case IsFindPic(point) => Result.Success()
      case NoFindPic()      => Result.Execution(Commands())
    }
  }

  def checkMpEmpty = RecAction { implicit c =>
    val result = Find.mpEmpty.run()
    result match {
      case IsFindPic(point) =>
        logger.warn("mp empty in war;")
        Result.Failure(EmEmptyException())
      case NoFindPic()      => Result.Execution(Commands())
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

  override def receive: Receive = {
    case c: ClientRequest =>
      val result = Sequence.run(action)(c, sender())
      action = result
  }
}

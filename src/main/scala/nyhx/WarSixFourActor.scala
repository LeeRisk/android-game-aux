package nyhx

import akka.actor.Actor
import models.{ClientRequest, Commands, Point}
import nyhx.Actions.{findAndTouch, utilFind, utilFindAndTouch}
import org.slf4j.LoggerFactory
import Actions._

trait WarHelp {
  val logger = LoggerFactory.getLogger("war")

  def warPoint(point: Point) = (Sequence()
    next utilFind(10, Images.returns.toGoal)
    next RecAction(c => Result.Success(Commands().addTap(point).addDelay(1000)))
    next utilFind(10, Images.start.toGoal)
    next warStart
    next waitWarEnd
    next sureWarEnd
    )


  def warRead = (Sequence()
    next utilFind(10, Images.Adventure.grouping.toGoal)
    next touchUtilNoFind(Images.Adventure.grouping.toGoal,e=>e)
    next checkMp
    next utilFindAndTouch(10, Images.start.toGoal)
    )

  def checkMp = RecAction { implicit x =>
    Find(Images.Adventure.mpEmpty.toGoal) match {
      case Some(point) => Result.Failure(EmEmptyException())
      case None        =>
        logger.info("check mp success")
        Result.Success()
    }
  }

  def warStart = RecAction { implicit x =>
    Find(Images.start.toGoal) match {
      case Some(point) => Result.Execution(Commands().addTap(point))
      case None        => Result.Success()
    }
  }

  def waitWarEnd = utilFind(100, Images.Adventure.totalTurn.toGoal)

  def sureWarEnd = RecAction { implicit c =>
    Find(Images.returns.toGoal) match {
      case Some(point) => Result.Success()
      case None        => Result.Execution(Commands().addTap(Point(0, 0)))
    }
  }

  def warReturn = (Sequence()
    next findAndTouch(Images.returns.toGoal)
    next utilFindAndTouch(10, Images.determine.toGoal)
    )

  def gotoWarMap(area: Point, zone: Int) = (Sequence()
    next touchReturns
    next goToRoom
    next utilFindAndTouch(10, Images.Adventure.adventure.toGoal)
    next utilFind(10, Images.Adventure.grouping.toGoal)
    next RecAction(c => Result.Success(Commands().addTap(Points.Area.one).addDelay(1000)))
    next RecAction(c => Result.Success(Commands().addTap(area).addDelay(1000)))
    next gotoAreaZone(zone)
    )

  def gotoAreaZone(zone: Int) = RecAction { implicit c =>
    Result.Success(
      (1 until zone).foldLeft(Commands())((acc, _) => acc.addTap(Points.Adventure.next).addDelay(1500))
    )
  }
}

class WarSixFourActor(warNum: Int = 100) extends Actor with WarHelp {

  var sequence = (Sequence()
    next gotoWarMap(Points.Area.six, 4)
    next (1 to warNum map (_ => warB) reduce (_ next _))
    next end
    )


  def warB = (Sequence()
    next warRead
    next warPoint(Points.Adventure.AreaSix.b)
    next warReturn
    )


  def end = RecAction { implicit e =>
    println("end")
    ???
  }


  override def receive: Receive = {
    case e: ClientRequest =>
      sequence = sequence.run(e, sender())

  }
}

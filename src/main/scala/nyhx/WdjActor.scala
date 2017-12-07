package nyhx

import akka.actor.{Actor, ActorRef}
import models._
import org.slf4j.LoggerFactory
import utensil.FindPic
import Actions._

object FindPoint {
  def apply(goal: GoalImage)(implicit clientRequest: ClientRequest) = {
    val fp = FindPic(clientRequest.image.toOriginal, goal)
    fp.point
  }

}

class WarSixFourActor() extends Actor {
  val logger = LoggerFactory.getLogger("war")

  var sequence = (Sequence()
    //    ~> touchReturns
    //    ~> goToRoom
    //    ~> utilFindAndTouch(10, Images.Adventure.adventure.toGoal)
    //    ~> utilFind(10, Images.Adventure.grouping.toGoal)
    //    ~> RecAction(c => Result.Success(Commands().addTap(Points.Area.one).addDelay(1000)))
    //    ~> RecAction(c => Result.Success(Commands().addTap(Points.Area.six).addDelay(1000)))
    //    ~> utilFind(10, Images.Area.six.toGoal)
    //
    //    ~> findAndTouch(Images.Adventure.grouping.toGoal)
    //    ~> utilFindAndTouch(10, Images.start.toGoal)
    next warPoint(Points.Adventure.AreaSix.b)
    next end
    )

  def warPoint(point: Point) = (Sequence()
    next utilFind(10, Images.returns.toGoal)
    next RecAction(c => Result.Success(Commands().addTap(point).addDelay(1000)))
    next startWar
    next waitWarEnd
    next sureWarEnd
    )

  def startWar = RecAction { implicit x =>
    FindPoint(Images.start.toGoal) match {
      case Some(point) => Result.Execution(Commands().addTap(point))
      case None        => Result.Success()
    }
  }

  def waitWarEnd = utilFind(100, Images.Adventure.totalTurn.toGoal)

  def sureWarEnd = RecAction { implicit c =>
    val a = FindPic(c.image.toOriginal, Images.returns.toGoal)
    if(a.isFind)
      Result.Success()
    else
      Result.Execution(Commands().addTap(Point(0, 0)))

  }

  def end = RecAction { implicit e =>
    println("end")
    ???
  }


  override def receive: Receive = {
    case e: ClientRequest =>
      sequence = sequence.run(e, sender())

  }
}

class WdjActor() extends Actor {
  val logger = LoggerFactory.getLogger("wjd")


  var sequence = (Sequence()
    next touchReturns
    next goToRoom
    next utilFind(10, Images.Wdj.wuDouJi.toGoal)
    next findAndTouch(Images.Wdj.wuDouJi.toGoal)

    next utilFind(10, Images.Wdj.shenShen.toGoal)
    next findAndTouch(Images.Wdj.shenShen.toGoal)

    ~> war
    ~> war
    next end
    )

  def war = (Sequence()
    next utilFind(10, Images.Wdj.matchBattle.toGoal)
    next findAndTouch(Images.Wdj.matchBattle.toGoal)

    next utilFind(60, Images.Wdj.fightResult.toGoal)
    next touchWarResult(Images.Wdj.fightResult.toGoal)
    )

  def end = RecAction { implicit e =>
    println("end")
    ???

  }

  def touchWarResult(goal: GoalImage) =
    FindPicAction("touch war result")
      .withIsFind(e => Result.Execution(Commands(TapCommand(0, 0))))
      .withNoFind(() => Result.Success())
      .withGoal(goal)
      .run()

  override def receive: Receive = {
    case e: ClientRequest =>
      sequence = sequence.run(e, sender())

  }


}

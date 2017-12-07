package nyhx

import akka.actor.{Actor, ActorRef}
import models._
import org.slf4j.LoggerFactory
import utensil.FindPic
import Actions._

trait Condition
trait Find extends Condition
trait IsFind extends Find
trait NoFind extends Find

class WarSixFourActor() extends Actor {
  val logger = LoggerFactory.getLogger("war")

  var sequence = (Sequence()
    ~> touchReturns
    ~> goToRoom
    ~> utilFindAndTouch(10, Images.Adventure.adventure.toGoal)
    ~> utilFind(10, Images.Adventure.grouping.toGoal)
    ~> RecAction(c => Result.Success(Commands().addTap(Points.Area.one).addDelay(1000)))
    ~> RecAction(c => Result.Success(Commands().addTap(Points.Area.six).addDelay(1000)))
    ~> utilFind(10, Images.Area.six.toGoal)
    ~> findAndTouch(Images.Adventure.grouping.toGoal)

    ~> warPoint(Points.Adventure.AreaSix.b)
    ~> end
    )

  def warPoint(point: Point) = (Sequence()
    ~> RecAction(c => Result.Success(Commands().addTap(point).addDelay(1000)))
    ~> utilFindAndTouch(10, Images.start.toGoal)
    ~> utilFind(100, Images.Adventure.totalTurn.toGoal)
    ~> touchUtilNoFind(Point(0, 0), Images.returns.toGoal)
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

class WdjActor() extends Actor {
  val logger = LoggerFactory.getLogger("wjd")


  var sequence = (Sequence()
    ~> touchReturns
    ~> goToRoom
    ~> utilFind(10, Images.Wdj.wuDouJi.toGoal)
    ~> findAndTouch(Images.Wdj.wuDouJi.toGoal)

    ~> utilFind(10, Images.Wdj.shenShen.toGoal)
    ~> findAndTouch(Images.Wdj.shenShen.toGoal)

    ~> war
    ~> war
    ~> end
    )

  def war = (Sequence()
    ~> utilFind(10, Images.Wdj.matchBattle.toGoal)
    ~> findAndTouch(Images.Wdj.matchBattle.toGoal)

    ~> utilFind(60, Images.Wdj.fightResult.toGoal)
    ~> touchWarResult(Images.Wdj.fightResult.toGoal)
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

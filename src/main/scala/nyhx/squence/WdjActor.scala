package nyhx.squence

import akka.actor.Actor
import models._
import nyhx.{Images, RecAction, Result}
import nyhx.squence.Actions._
import org.slf4j.LoggerFactory
import utensil.FindPic

object Find {
  def apply(goal: GoalImage)(implicit clientRequest: ClientRequest) = {
    val fp = FindPic(clientRequest.image.toOriginal, goal)
    fp.point
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

    next war
    next war
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

package nyhx.sequence

import scala.language.implicitConversions

import akka.actor.Actor
import models._
import nyhx.Images
import nyhx.sequence.Find.findPicBuilding2FindAux
import utensil.{IsFindPic, NoFindPic}

class DismissedActor extends Actor
  with ActorHelper
  with BaseHelper
  with ScenesHelper {
  val sequences = (Sequence("dismissed")
    next touchReturns
    next goToGruen
    next Find(Images.YuanZiWu.yuanZiWu.toGoal).touch
    next justDelay(1000)
    next Find(Images.YuanZiWu.dismissed.toGoal).touch
    repeat(execDismissed, 10)
    )

  def execDismissed: Sequence = (Sequence("exec dismissed")
    next Find(Images.YuanZiWu.selectStudent.toGoal).touch
    next Find(Images.Retrieve.retrieve.toGoal).touch
    next Find(Images.Retrieve.an.toGoal).touch
    next Find(Images.Retrieve.shui.toGoal).touch
    next justTap(Point(1, 1), 1000)
    next selectStudent
    next checkNeedDismissed
    next Find(Images.YuanZiWu.dismissedDetermine.toGoal).touch
    next justTap(Point(1, 1), 1000)
    )

  def selectStudent = RecAction { implicit c =>
    val points = 0 to 5 map (_ * 175 + 65) map (x => Point(x, 179))
    val commands = points.foldLeft(Commands())((l, r) =>
      l.addTap(r).addDelay(200)
    )
    val result = Find(Images.lv1.toGoal)(c).run()
    if(result.noFind)
      Result.Success()
    else {
      Result.Success(commands)
    }
  }

  def checkNeedDismissed = RecAction { implicit c =>
    val result = Find(Images.YuanZiWu.dismissedSelectStudentDetermine.toGoal)(c).run()
    result match {
      case IsFindPic(point) =>
        Result.Success(Commands().addTap(point))
      case NoFindPic()      =>
        Result.Become(end)
    }
  }


  def end = RecAction { implicit c =>
    context.parent ! DismissedTaskFinish(self)
    println("dismissed  end")
    Result.End()
  }
}

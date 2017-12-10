package nyhx.sequence

import scala.language.implicitConversions

import akka.actor.Actor
import models._
import nyhx._
import Find.findPicBuilding2FindAux
import utensil.{IsFindPic, NoFindPic}

class WarAreaSixActor
  extends Actor
    with ActorHelper
    with BaseHelper
    with ScenesHelper
    with WarHelper {


  val sequences: Sequence = (Sequence("war")
    next goToAdventure
    next goToWarArea(Points.Area.six, 4)
    repeat(warPoint_B, 100)
    )


  def warPoint_B = {
    // goto adventure
    // war point(B)
    // exit war
    (Sequence("warPoint_B")
      next warReady
      next warPoint(Points.Adventure.AreaSix.One.b)
      next warEnd
      )
  }

  def end = RecAction { implicit c => println("end"); ??? }

}

class WarAreaThreeOneActor(warNum: Int = 100) extends Actor
  with ActorHelper
  with BaseHelper
  with ScenesHelper
  with WarHelper {


  val sequences: Sequence = (Sequence("war")
    next goToAdventure
    next goToWarArea(Points.Area.three, 1)
    repeat(warPoint_b_e, warNum)
    next end
    )

  def warPoint_b_e = (Sequence("")
    next warReady
    next warPoint(Points.Adventure.AreaThree.One.b)
    next warPoint(Points.Adventure.AreaThree.One.e)
    next warEnd
    )

  def end = RecAction { implicit c =>
    println("WarAreaThreeOneActor end")
    context.parent ! WarTaskEnd(self)
    Result.End()
  }


}

class WarAreaFiveOneActor(warNum: Int) extends Actor
  with ActorHelper
  with BaseHelper
  with ScenesHelper
  with WarHelper {
  val sequences: Sequence =
//    Sequence("") next randomPoint(Point(275, 235))
  (Sequence("war")
    next goToAdventure
    next goToWarArea(Points.Area.five, 1)
    repeat(warPoint_f, warNum)
    next end
    )

  def warPoint_f = (Sequence("warPoint_f")
    next warReady
    next warPoint(Points.Adventure.Five.One.b)
    next warPoint(Points.Adventure.Five.One.c)
    next warPoint(Points.Adventure.Five.One.d)
    next randomPoint(Points.Adventure.Five.One.e)
    next warPoint(Points.Adventure.Five.One.f)
    next justDelay(3000)
    next warEnd
    )

  def end = RecAction { implicit c =>
    println("WarAreaThreeOneActor end")
    context.parent ! WarTaskEnd(self)
    Result.End()
  }
}



class WarAreaThreeSixActor(warNum: Int) extends Actor
  with ActorHelper
  with BaseHelper
  with ScenesHelper
  with WarHelper {
  val sequences: Sequence =
  //    Sequence("") next randomPoint(Point(275, 235))
    (Sequence("war")
      next goToAdventure
      next goToWarArea(Points.Area.three, 6)
      repeat(warPoint_h, warNum)
      next end
      )

  def warPoint_h = (Sequence("warPoint_h")
    next warReady
    next warPoint(Points.Adventure.Three.Six.b)
    next warPoint(Points.Adventure.Three.Six.c)
    next randomPoint(Points.Adventure.Three.Six.g)
    next warPoint(Points.Adventure.Three.Six.h)
    next warEnd
    )

  def end = RecAction { implicit c =>
    println("WarAreaThreeOneActor end")
    context.parent ! WarTaskEnd(self)
    Result.End()
  }
}

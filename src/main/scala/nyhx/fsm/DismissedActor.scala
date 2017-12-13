package nyhx.fsm

import akka.actor.{FSM, Props}
import models.{ClientRequest, Commands, Point}
import nyhx.fsm.FindActor.IsFind
import nyhx.{Find, Images}
import Find.FindPicBuildingWithRun
import utensil.{IsFindPic, NoFindPic}

object DismissedActor {

  trait Status extends BaseStatus

  object SelectStudent extends Status

  object TouchSelect extends Status

  object TouchRetrieve extends Status

  object SureRetrieve extends Status

  object TapStudent extends Status

  object Finish extends Status


  object Move extends Status

  object Determine extends Status

  object DetermineDetermine extends Status

  type Data = BaseData
}

import DismissedActor._

/**
  * goto gruen
  * touch yzw
  * touch dismissed
  * touch select student
  * select student
  * find determine
  * if is find then
  * touch it
  * touch determine sure
  * goto select student
  * else
  * return
  */
class DismissedActor extends FSM[Status, Data]
  with FsmHelper[Status, Data] {

  import context.actorOf

  def moveActors() = List(
    actorOf(ScenesActor.returns),
    actorOf(ScenesActor.gotoGruen),
    actorOf(FindActor.touch(Find(Images.YuanZiWu.yuanZiWu))),

    actorOf(JustActor.justDelay(3000)),

    actorOf(FindActor.waitFind(FindActor.IsFind, Find(Images.YuanZiWu.dismissed))),
    actorOf(FindActor.touch(Find(Images.YuanZiWu.dismissed))),

  )

  def dismissedSelectActor() = List(
    context actorOf Props(new DismissedSelectActor)
  )

  def dismissedDetermineActor() = List(
    context actorOf FindActor.touch(Find(Images.YuanZiWu.dismissedDetermine))
  )

  startWith(Move, WorkActorList(moveActors()))
  when(Move)(work(goto(SelectStudent).using(WorkActorList(dismissedSelectActor()))))
  when(SelectStudent)(work(goto(Determine)))
  when(Determine) {
    case Event(c: ClientRequest, _) =>
      val result = Find(Images.YuanZiWu.dismissedSelectStudentDetermine).run(c)
      if(result.isFind)
        goto(DetermineDetermine)
          .using(WorkActorList(dismissedDetermineActor()))
          .replying(Commands().addTap(result.point))
      else
        goto(Finish).replying(Commands())
  }
  when(DetermineDetermine)(work(goto(SelectStudent).using(WorkActorList(dismissedSelectActor()))))


  when(Finish) {
    case _ =>
      log.warning("dismiss select actor finish")
      stay()
  }
  onTransition {
    case x -> Finish => context.parent ! TaskFinish
  }
}

class DismissedSelectActor extends FSM[Status, Data]
  with FsmHelper[Status, Data] {

  import context.actorOf

  def touchSelect() = List(
    context actorOf FindActor.waitFind(IsFind, Find(Images.YuanZiWu.selectStudent)),
    context actorOf FindActor.touch(Find(Images.YuanZiWu.selectStudent))
  )

  def touchRetrieve() = List(
    context actorOf FindActor.waitFind(IsFind, Find(Images.Retrieve.retrieve)),
    context actorOf FindActor.touch(Find(Images.Retrieve.retrieve)),

    context actorOf FindActor.waitFind(IsFind, Find(Images.Retrieve.an)),
    context actorOf FindActor.touch(Find(Images.Retrieve.an)),

    context actorOf FindActor.waitFind(IsFind, Find(Images.Retrieve.shui)),
    context actorOf FindActor.touch(Find(Images.Retrieve.shui))
  )

  startWith(TouchSelect, WorkActorList(touchSelect()))
  when(TouchSelect)(work(goto(TouchRetrieve).using(WorkActorList(touchRetrieve()))))
  when(TouchRetrieve)(work(goto(SureRetrieve).using(NoData)))
  when(SureRetrieve) {
    case Event(c: ClientRequest, _) =>
      Find(Images.Retrieve.attributes).run(c) match {
        case IsFindPic(point) => stay().replying(Commands().addTap(Point(1, 1)))
        case NoFindPic()      => goto(TapStudent).replying(Commands().addDelay(0))
      }
  }
  when(TapStudent) {
    case Event(c: ClientRequest, _) =>
      val points = 0 to 1 map (_ * 175 + 65) map (x => Point(x, 179))
      val commands = points.foldLeft(Commands())((l, r) =>
        l.addTap(r).addDelay(500)
      )

      goto(Finish).replying(commands)
  }
  when(Finish) {
    case _ =>
      log.warning("dismiss select actor finish")
      stay()
  }
  onTransition {
    case x -> Finish => context.parent ! TaskFinish
  }
}




































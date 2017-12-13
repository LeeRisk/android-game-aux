package nyhx.fsm

import akka.actor.{Actor, ActorRef, FSM, Props}
import models.{ClientRequest, Commands, Point}
import nyhx.{Find, Images}
import org.slf4j.LoggerFactory
import utensil.{IsFindPic, NoFindPic}


object WdjActor {

  trait Status extends BaseStatus

  object Move extends Status

  object War extends Status

  object StartWar extends Status

  object WaitWarEnd extends Status

  object SureWarResult extends Status

  object Finish extends Status

  type Data = BaseData

  case class WarNum(num: Int, actorRef: ActorRef) extends Data

}

import nyhx.fsm.WdjActor._


class WdjWarActor extends FSM[Status, Data]
    with FsmHelper[Status, Data] {

  import context.actorOf

  val logger = LoggerFactory.getLogger("wdj-war")

  def startWar = List(
    actorOf(FindActor.keepTouch(Find(Images.Wdj.matchBattle)))
  )

  def waitWarEnd = List(
    actorOf(FindActor.waitFind(FindActor.IsFind, Find(Images.Wdj.fightResult)))
  )


  startWith(StartWar, WorkActorList(startWar))
  when(StartWar)(work(nextState = goto(WaitWarEnd).using(WorkActorList(waitWarEnd))))
  when(WaitWarEnd)(work(nextState = goto(SureWarResult).using(NoData)))
  when(SureWarResult) {
    case Event(c: ClientRequest, _) =>
      Find(Images.Wdj.fightResult)(c).run() match {
        case IsFindPic(point) =>
          logger.info("sure war result")
          stay().replying(Commands().tap(Point(1, 1)))
        case NoFindPic()      =>
          logger.info("war end")
          context.parent ! TaskFinish
          goto(Finish).replying(Commands())
      }
  }
  when(Finish) { case _ => logger.info("finish"); stay() }

  implicit def workActorListData(workActorList: WorkActorList): Data = workActorList
}

class WdjActor(totalWarNum: Int = 10) extends FSM[Status, Data]
    with FsmHelper[Status, Data] {

  import context.actorOf

  def moveActors() = List(
    actorOf(ScenesActor.returns),
    actorOf(ScenesActor.gotoRoom),
    actorOf(FindActor.touch(Find(Images.Wdj.wuDouJi))),
    actorOf(FindActor.waitFind(FindActor.IsFind, Find(Images.returns))),
    actorOf(FindActor.touch(Find(Images.Wdj.shenShen))),
  )

  def warActor() = actorOf(Props(new WdjWarActor))


  startWith(Move, WorkActorList(moveActors()))
  when(Move)(work(nextState = goto(War).using(WarNum(0, warActor()))))
  when(War) {
    case Event(c: ClientRequest, WarNum(i, actorRef))               =>
      actorRef forward c
      stay()
    case Event(TaskFinish, WarNum(i, actorRef)) if i < totalWarNum  =>
      logger.info(s"$i/$totalWarNum - end")
      context.stop(actorRef)
      goto(War).using(WarNum(i + 1, warActor()))
    case Event(TaskFinish, WarNum(i, actorRef)) if i >= totalWarNum =>
      context.stop(actorRef)
      goto(Finish)

  }
  when(Finish) { case _ =>
    logger.info("finish")
    stay()
  }


  val logger = LoggerFactory.getLogger("wdj")
}

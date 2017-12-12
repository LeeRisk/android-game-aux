package nyhx.fsm

import akka.actor.{Actor, ActorRef, FSM, Props}
import models.{ClientRequest, Commands}
import nyhx.Images
import nyhx.sequence.Find


object WdjActor {

  trait Status extends BaseStatus

  object Move extends Status

  object StartWar extends Status

  object WaitWarEnd extends Status

  object SureWarResult extends Status

  type Data = BaseData
}


import WdjActor._

class WdjActor extends Actor with FSM[Status, Data] {
  def scenesMoveActor: ActorRef = context.actorOf(ScenesMoveActor(
    ScenesStatus.Returns,
    ScenesStatus.GotoRoom,
    ScenesStatus.GotoWdj
  ))

  startWith(Move, WorkActor(scenesMoveActor))
  when(Move) {
    case Event(c: ClientRequest, WorkActor(actorRef)) =>
      actorRef forward c
      stay()
    case Event(TaskFinish, WorkActor(actorRef))       =>
      context.stop(actorRef)
      goto(StartWar).using(NoData)
  }

  //  startWith(ScenesActor.Returns, WorkActor(context.actorOf(Props(new ScenesActor(Returns)))))
  //  when(ScenesActor.Returns) {
  //    case Event(c: ClientRequest, WorkActor(actorRef)) =>
  //      actorRef forward c
  //      stay()
  //    case Event(TaskFinish, WorkActor(actorRef))       =>
  //      context.stop(actorRef)
  //      goto(ScenesActor.GotoRoom)
  //        .using(WorkActor(context.actorOf(Props(new ScenesActor(ScenesActor.GotoRoom)))))
  //  }

  //  onTransition()
  // goto wdj
  // goto shenshen
  // goto match battle
  // wait fight result
  // sure result

  Find(Images.returns.toGoal)
  Find(Images.returns_room.toGoal)
  Find(Images.Wdj.wuDouJi.toGoal)
}

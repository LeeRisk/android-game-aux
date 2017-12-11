package nyhx.fsm

import akka.actor.{Actor, ActorRef, FSM, Props}
import models.{ClientRequest, Commands}
import nyhx.Images
import nyhx.sequence.Find
import Find.findPicBuilding2FindAux

class ReturnsActor extends Actor {
  override def receive: Receive = {
    case c: ClientRequest =>
      val result = Find(Images.returns.toGoal)(c).run()
      if(result.isFind) {
        sender() ! Commands().addTap(result.point)
      } else {
        sender() ! Commands()
        context.parent ! TaskFinish
      }
  }
}

object SimplePatten {

//  import akka.typed._
//  import akka.typed.scaladsl.Actor
//  import akka.typed.scaladsl.AskPattern._

}

object TaskFinish

object WdjActor {

  trait Status

  case object Return extends Status

  case object Room extends Status

  case object Wdj extends Status

  case object ShenShen extends Status

  case object Match extends Status

  case object FightResult extends Status


  trait Data

  case class WorkActor(actorRef: ActorRef) extends Data

}


import WdjActor._


class WdjActor extends Actor with FSM[Status, Data] {

  startWith(Return, WorkActor(context.actorOf(Props(new ReturnsActor))))
  when(Return) {
    case Event(c: ClientRequest, WorkActor(actorRef)) =>
      actorRef forward c
      stay()
    case Event(TaskFinish, WorkActor(actorRef))       =>
      context.stop(actorRef)
      goto(Room)
  }

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

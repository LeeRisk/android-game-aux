package nyhx.fsm

import akka.actor.{Actor, ActorRef, FSM, LoggingFSM, Props}
import models.{ClientRequest, Commands, NoFindPicException}
import nyhx.Images
import nyhx.sequence.Find
import utensil.{IsFindPic, NoFindPic}
import nyhx.sequence.Find.findPicBuildingWithRun
import org.slf4j.LoggerFactory
import nyhx.fsm.ScenesStatus._

//class ReturnsActor extends Actor {
//  override def receive: Receive = {
//    case c: ClientRequest =>
//      val result = Find(Images.returns.toGoal)(c).run()
//      if(result.isFind) {
//        sender() ! Commands().addTap(result.point)
//      } else {
//        sender() ! Commands()
//        context.parent ! TaskFinish
//      }
//  }
//}

object ScenesMoveActor {
  def apply(seq: ScenesStatus*): Props = Props(new ScenesMoveActor(seq))
}

class ScenesMoveActor(seq: Seq[ScenesStatus]) extends Actor {
  private var workActorList = seq.map(e => context.actorOf(Props(new ScenesActor(e))))

  override def receive: Receive = {
    case c: ClientRequest => workActorList.head forward c
    case TaskFinish       =>
      context.stop(workActorList.head)
      workActorList = workActorList.tail
      if(workActorList.isEmpty) context.parent ! TaskFinish

  }
}


class ScenesActor(goal: ScenesStatus) extends Actor with FSM[ScenesStatus, BaseData] {
  val regulator: ActorRef = context.parent

  startWith(goal, UnInit)

  when(Returns) {
    case Event(c: ClientRequest, _) =>
      logger.debug("at return")
      Find(Images.returns.toGoal)(c).run() match {
        case IsFindPic(point) => stay().replying(Commands().addTap(point))
        case NoFindPic()      => goto(Finish).replying(Commands())
      }
  }

  when(GotoRoom) {
    case Event(c: ClientRequest, _) =>
      val adventure = Find(Images.Adventure.adventure).run(c)
      val gotoRoom = Find(Images.returns_room).run(c)
      (adventure.isFind, gotoRoom.isFind) match {
        case (false, true)  => stay().replying(Commands().addTap(gotoRoom.point))
        case (true, _)      => goto(Finish).replying(Commands())
        case (false, false) => goto(Failure).replying(Commands())
      }
  }

  when(GotoWdj) {
    case Event(c: ClientRequest, _) =>
      val wdj = Find(Images.Wdj.wuDouJi).run(c)
      wdj match {
        case IsFindPic(point) => goto(Finish).replying(Commands().addTap(point))
        case NoFindPic()      => goto(Failure).replying(Commands())
      }
  }

  when(Failure) {
    case Event(_, _) =>
      logger.error("scenes move is failure")
      stay()
  }
  when(Finish) {
    case _ =>
      logger.error("scenes move is finish")
      stay()
  }


  val logger = LoggerFactory.getLogger("scenes-move")
  onTransition {
    case x -> Failure => regulator ! TaskFailure(NoFindPicException(x.toString))
    case x -> Finish  =>
      logger.info("[finish] go to scenes : " + x.toString)
      regulator ! TaskFinish
  }
}
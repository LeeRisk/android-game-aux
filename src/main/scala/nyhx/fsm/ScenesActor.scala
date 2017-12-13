package nyhx.fsm

import akka.actor.{Actor, ActorRef, FSM, Props}
import models.{ClientRequest, Commands, NoFindPicException}
import nyhx.{Find, Images}
import nyhx.Find.FindPicBuildingWithRun
import org.slf4j.LoggerFactory
import utensil.{IsFindPic, NoFindPic}


object ScenesActor {

  trait Status

  object Returns extends Status

  object GotoRoom extends Status

  object GotoGruen extends Status

  object Failure extends Status

  object Finish extends Status


  def returns = Props(new ScenesActor(Returns))

  def gotoRoom = Props(new ScenesActor(GotoRoom))

  def gotoGruen = Props(new ScenesActor(GotoGruen))
}

import nyhx.fsm.ScenesActor._

class ScenesActor(goal: Status) extends Actor with FSM[Status, BaseData] {
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
  when(GotoGruen) {
    case Event(c: ClientRequest, _) =>
      val gotoGakuen = Find(Images.returns_gakuen).run(c)
      gotoGakuen match {
        case IsFindPic(point) => goto(Finish).replying(Commands().addTap(point))
        case NoFindPic()      => goto(Finish).replying(Commands())
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
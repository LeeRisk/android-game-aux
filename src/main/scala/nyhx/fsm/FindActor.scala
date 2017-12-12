package nyhx.fsm

import akka.actor.{Actor, ActorRef, FSM, Props}
import models.{ClientRequest, Commands, NoFindPicException}
import org.slf4j.{Logger, LoggerFactory}
import utensil.{FindPicBuild, IsFindPic, NoFindPic}
import nyhx.Find._

object FindActor {


  trait Status extends BaseStatus

  object Touch extends Status

  object KeepTouch extends Status

  object WaitFind extends Status

  object FailureNoFind extends Status

  object FailureNoSupper extends Status

  object Success extends Status

  trait Condition

  object IsFind extends Condition

  object NoFind extends Condition

  object Nothing extends Condition

  type Func = ClientRequest => FindPicBuild[FindPicBuild.Request]

  def touch(f: Func) = Props(new FindActor(Touch, Nothing, f))

  def keepTouch(f: Func) = Props(new FindActor(KeepTouch, Nothing, f))

  def waitFind(condition: Condition, f: Func) = Props(new FindActor(WaitFind, condition, f))
}

import FindActor._

class FindActor(status: FindActor.Status,
                condition: Condition,
                findPicBuild: ClientRequest => FindPicBuild[FindPicBuild.Request])
  extends Actor
    with FSM[FindActor.Status, FindActor.Condition] {
  val logger: Logger = LoggerFactory.getLogger("find-aux")

  startWith(status, condition)

  when(Touch) {
    case Event(c: ClientRequest, Nothing) =>
      val goal = findPicBuild(c).goal.get.simpleName
      findPicBuild.run(c) match {
        case NoFindPic()      => goto(FailureNoFind).replying(Commands())
        case IsFindPic(point) =>
          logger.info(s"($goal) is find; touch")
          goto(Success).replying(Commands().addTap(point))
      }
    case Event(c: ClientRequest, _)       =>
      logger.error("no supper")
      goto(FailureNoFind)
  }
  when(KeepTouch) {
    case Event(c: ClientRequest, Nothing) =>
      val goal = findPicBuild(c).goal.get.simpleName
      findPicBuild.run(c) match {
        case NoFindPic()      => goto(Success).replying(Commands())
        case IsFindPic(point) =>
          logger.info(s"($goal) is find; keep touch")
          stay().replying(Commands().addTap(point))
      }
  }

  when(WaitFind) {
    case Event(c: ClientRequest, IsFind) =>
      val goal = findPicBuild(c).goal.get.simpleName
      findPicBuild.run(c) match {
        case NoFindPic()      =>
          logger.info(s"wait find ($goal) no find , re try")
          stay().replying(Commands())
        case IsFindPic(point) =>
          logger.info(s"wait find ($goal) is find , success")
          goto(Success).replying(Commands())
      }
    case Event(c: ClientRequest, NoFind) =>
      findPicBuild.run(c) match {
        case IsFindPic(_) => stay().replying(Commands())
        case NoFindPic()  => goto(Success).replying(Commands())
      }
  }

  def regulator: ActorRef = context.parent

  when(FailureNoFind) {
    case Event(_, _) =>
      logger.error("scenes move is failure")
      stay()
  }
  when(FailureNoSupper) {
    case Event(_, _) =>
      logger.error("failure no supper")
      stay()
  }
  when(Success) {
    case _ =>
      logger.error("scenes move is finish")
      stay()
  }

  def statusToString(s: Status) = s.getClass.getName.replace("$", ".")

  onTransition {
    case x -> FailureNoFind   => regulator ! TaskFailure(NoFindPicException(statusToString(x)))
    case x -> FailureNoSupper => regulator ! TaskFailure(new Exception(x.toString + s" with(${stateData.getClass.getName}) is no supper"))
    case x -> Success         => regulator ! TaskFinish
      logger.debug("[finish] : " + (x))
  }
  onTransition {
    case f -> t =>
      logger.debug(s"status:${(f)} -> ${(t)}")
  }
}
package nyhx

import akka.actor.{Actor, ActorRef, FSM, Props}
import models.ClientRequest
import nyhx.fsm.{TaskFinish, WdjActor}
import nyhx.sequence._
import org.slf4j.LoggerFactory

object ClientActor {

  trait Status

  case object War extends Status

  case object Dismissed extends Status

  trait Data

  case class WorkActor(actorRef: ActorRef) extends Data

}

import nyhx.ClientActor._

class ClientActor(args: Seq[String]) extends Actor with FSM[ClientActor.Status, ClientActor.Data] {

  import context.actorOf

  val logger = LoggerFactory.getLogger("client-actor")
  val warNum = 50

  def mkDismissed() = context.actorOf(Props(new DismissedActor()))

  def contains(s: String) = args.contains(s.trim)

  def statusMap() = {
    val map = Map[Status, () => Data](
      //default value
      (War -> (() => WorkActor(actorOf(Props(new WarAreaSixActor()))))),
      (Dismissed -> (() => WorkActor(actorOf(Props(new nyhx.fsm.DismissedActor)))))
    )

    args.foldLeft(map) {
      case (acc, "war-2-6") => map + (War -> (() => WorkActor(actorOf(Props(new WarAreaTwoSix(warNum))))))
      case (acc, "war-6-4") => map + (War -> (() => WorkActor(actorOf(Props(new WarAreaSixActor())))))
      case (acc, "wdj")     => map + (War -> (() => WorkActor(actorOf(Props(new WdjActor(warNum))))))
      case (acc, "dismiss") => map + (Dismissed -> (() => WorkActor(actorOf(Props(new nyhx.fsm.DismissedActor)))))
      case _                => map
    }
  }

  val map = statusMap()
  startWith(Dismissed, map(Dismissed)())
  when(War) {
    case Event(x: ClientRequest, WorkActor(actorRef)) =>
      actorRef forward x
      stay()
    case Event(WarTaskEnd(_), WorkActor(actorRef))    =>
      logger.info("war finish go to dismissed")
      context.stop(actorRef)
      goto(Dismissed) using map(Dismissed)()
  }

  when(Dismissed) {
    case Event(x: ClientRequest, WorkActor(actorRef)) =>
      actorRef forward x
      stay()
    case Event(TaskFinish, WorkActor(actorRef))       =>
      logger.info("dismissed finish go to war")
      context.stop(actorRef)
      goto(War).using(map(War)())
  }
}

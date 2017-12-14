package nyhx

import akka.actor.{Actor, ActorRef, FSM, Props}
import models.ClientRequest
import nyhx.fsm.{DismissedActor => _, _}
import nyhx.sequence._
import org.slf4j.LoggerFactory

object ClientActor {

  trait Status extends BaseStatus

  case object War extends Status

  case object Dismissed extends Status

  type Data = BaseData


}

import nyhx.ClientActor._

class ClientActor(args: Seq[String]) extends FSM[Status, Data] with FsmHelper[Status, Data] {

  import context.actorOf

  val logger = LoggerFactory.getLogger("client-actor")
  val warNum = 1

  def mkDismissed() = context.actorOf(Props(new DismissedActor()))

  def contains(s: String) = args.contains(s.trim)

  def statusMap() = {
    val map = Map(
      //default value
      War -> (() => actorOf(Props(new WarAreaSixActor(warNum)))),
      Dismissed -> (() => actorOf(Props(new nyhx.fsm.DismissedActor)))
    )

    args.foldLeft(map) {
      case (acc, "war-2-6") => acc + (War -> (() => actorOf(Props(new WarAreaTwoSix(warNum)))))
      case (acc, "war-6-4") => acc + (War -> (() => actorOf(Props(new WarAreaSixActor(warNum)))))
      case (acc, "wdj")     => acc + (War -> (() => actorOf(Props(new WdjActor(warNum)))))
      case (acc, "dismiss") => acc + (Dismissed -> (() => actorOf(Props(new nyhx.fsm.DismissedActor))))
      case (acc, _)         => acc
    }
  }

  val map = statusMap()
  startWith(War, map(War)())
  when(War)(work(nextStatus = goto(Dismissed).using(map(Dismissed)())))
  when(Dismissed)(work(nextStatus = goto(War).using(map(War)())))

}

package nyhx

import akka.actor.{Actor, ActorRef, FSM, Props}
import models.ClientRequest
import nyhx.fsm.WdjActor
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

  def startStatus() = {
    /**/ if(contains("war-2-6")) startWith(War, WorkActor(actorOf(Props(new WarAreaTwoSix(warNum)))))
    else if(contains("war-6-4")) startWith(War, WorkActor(actorOf(Props(new WarAreaSixActor()))))
    else if(contains("wdj    ")) startWith(War, WorkActor(actorOf(Props(new WdjActor(warNum)))))
  }

  startStatus()
  when(War) {
    case Event(x: ClientRequest, WorkActor(actorRef)) =>
      actorRef forward x
      stay()
    case Event(WarTaskEnd(_), WorkActor(actorRef))    =>
      logger.info("war finish go to dismissed")
      context.stop(actorRef)
      goto(Dismissed) using WorkActor(mkDismissed())
  }

  when(Dismissed) {
    case Event(x: ClientRequest, WorkActor(actorRef))       =>
      actorRef forward x
      stay()
    case Event(DismissedTaskFinish(_), WorkActor(actorRef)) =>
      logger.info("dismissed finish go to war")
      context.stop(actorRef)
      stay()
//      goto(War) using WorkActor(mkWar())
  }
}

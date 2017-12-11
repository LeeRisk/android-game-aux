package nyhx

import akka.actor.{Actor, ActorRef, FSM, Props}
import models.{ClientRequest, DismissedTaskFinish, WarTaskEnd}
import nyhx.sequence._
import org.slf4j.LoggerFactory

object ClientActor {

  trait Status

  case object War extends Status

  case object Dismissed extends Status

  trait Data

  case class ActorRefData(actorRef: ActorRef) extends Data

}

import ClientActor._

class ClientActor() extends Actor with FSM[ClientActor.Status, ClientActor.Data] {


  val logger = LoggerFactory.getLogger("client-actor")
  val warNum = 50

  def mkDismissed() = context.actorOf(Props(new DismissedActor()))

  def mkWar() = context.actorOf(Props(new WarAreaTwoSix(warNum)))

  startWith(War, ActorRefData(mkWar()))

  when(War) {
    case Event(x: ClientRequest, ActorRefData(actorRef)) =>
      actorRef forward x
      stay()
    case Event(WarTaskEnd(_), ActorRefData(actorRef))    =>
      logger.info("war finish go to dismissed")
      context.stop(actorRef)
      goto(Dismissed) using ActorRefData(mkDismissed())
  }

  when(Dismissed) {
    case Event(x: ClientRequest, ActorRefData(actorRef))       =>
      actorRef forward x
      stay()
    case Event(DismissedTaskFinish(_), ActorRefData(actorRef)) =>
      logger.info("dismissed finish go to war")
      context.stop(actorRef)
      goto(War) using ActorRefData(mkWar())
  }
}

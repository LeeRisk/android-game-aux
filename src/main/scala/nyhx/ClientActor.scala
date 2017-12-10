package nyhx

import akka.actor.{Actor, ActorRef, FSM, Props}
import models.{ClientRequest, DismissedTaskFinish, WarTaskEnd}
import nyhx.sequence.{DismissedActor, WarAreaFiveOneActor, WarAreaThreeOneActor, WarAreaThreeSixActor}
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
  val warNum = 1

  def mkDismissed() = context.actorOf(Props(new DismissedActor()))

  def mkWar() = context.actorOf(Props(new WarAreaThreeOneActor(warNum)))

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

//class ClientActor() extends Actor {
//  val logger = LoggerFactory.getLogger("client-actor")
//  val warNum = 10
//
//  def mkDismissed() = context.actorOf(Props(new DismissedActor()))
//
//  def mkWar() = context.actorOf(Props(new WarAreaThreeOneActor(warNum)))
//
//  //  def mkWar() = context.actorOf(Props(new WarAreaThreeSixActor(warNum)))
//
//  //  var work = mkDismissed()
//  var work = mkWar()
//
//  override def receive = war
//
//  def war: Receive = {
//    case x@ClientRequest(screen) =>
//      logger.debug(s"receive screen file :${x.image.name}")
//      work.forward(x)
//    case WarTaskEnd(actorRef)    =>
//      logger.info(s"war $warNum finish")
//      context.stop(actorRef)
//      work = mkDismissed()
//      context.become(dismissed)
//  }
//
//  def dismissed: Receive = {
//    case x@ClientRequest(screen)       =>
//      logger.debug(s"receive screen file :${x.image.name}")
//      work.forward(x)
//    case DismissedTaskFinish(actorRef) =>
//      logger.info(" [controller ] dismissed finish")
//      context.stop(actorRef)
//      work = mkWar()
//      context.become(war)
//  }
//
//}
//

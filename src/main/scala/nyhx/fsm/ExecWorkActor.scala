package nyhx.fsm

import akka.actor.{Actor, ActorRef, Props}
import models.ClientRequest

class ExecWorkActor(var seq: Seq[ActorRef]) extends Actor {
  override def receive: Receive = {
    case c: ClientRequest =>
      seq.head forward c
    case TaskFinish       =>
      if(seq.tail.isEmpty)
        context.parent ! TaskFinish
      else {
        seq = seq.tail
      }
  }
}

object ExecWorkActor {
  def apply(seq: ActorRef*): Props = Props(new ExecWorkActor(seq))
}
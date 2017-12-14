package nyhx.fsm

import akka.actor.{Actor, ActorRef, Props}
import models.ClientRequest

class ExecWorkActor(val seq: Seq[Props]) extends Actor {
  var __seq = seq.map(context.actorOf)
  override def receive: Receive = {
    case c: ClientRequest =>
      __seq.head forward c
    case TaskFinish       =>
      if(__seq.tail.isEmpty)
        context.parent ! TaskFinish
      else {
        __seq = __seq.tail
      }
  }
}

object ExecWorkActor {
  def apply(seq: Props*): Props = Props(new ExecWorkActor(seq))
}
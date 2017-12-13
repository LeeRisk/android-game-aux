package nyhx.fsm

import akka.actor.{Actor, FSM, Props}
import models.{ClientRequest, Commands, Point}

trait FsmHelper[S, D] {
  this: FSM[S, D] =>

  def work(nextState: State)(implicit f: WorkActorList => D): StateFunction = {
    case Event(c: ClientRequest, WorkActorList(list)) =>
      list.head forward c
      stay()
    case Event(TaskFinish, x@WorkActorList(list))     =>
      context.stop(list.head)
      if(list.tail.isEmpty)
        nextState
      else
        stay().using(f(WorkActorList(list.tail)))
  }

  onTransition {
    case f -> t => log.debug(s"onTransition:$f -> $t")
  }
}

class JustActor(commands: Commands) extends Actor {
  override def receive: Receive = {
    case c: ClientRequest =>
      sender() ! commands
      context.parent ! TaskFinish
  }
}

object JustActor {
  def justDelay(time: Int) = Props(new JustActor(Commands().delay(time)))

  def justTap(point: Point) = Props(new JustActor(Commands().tap(point)))
}
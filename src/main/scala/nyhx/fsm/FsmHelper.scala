package nyhx.fsm

import akka.actor.{Actor, ActorRef, FSM, Props}
import models.{ClientRequest, Commands, Point}

trait FsmHelper[S, D] {
  this: FSM[S, D] =>

  class Build[A](private val state: State) {
    def replying(commands: Commands) =
      new Build[A with Build.Reply](state.replying(commands))

    def using(d: D) =
      new Build[A](state.using(d))
  }

  object Build {

    implicit class WithBuild[A](b: Build[A])(implicit x: A <:< Reply) {
      def build() = b.state
    }

    def apply(state: State) = new Build[Nothing](state)

    def stay() = new Build[Nothing](self_.stay())

    def goto(s: S) = new Build[Nothing](self_.goto(s))

    trait Reply

    trait Nothing

  }

  private val self_ = this


  @deprecated("use work")
  def workList(nextState: State)(implicit f: WorkActorList => D): StateFunction = {
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

  def work(nextStatus: => State)(implicit f: WorkActor => D): StateFunction = {
    case Event(c: ClientRequest, WorkActor(actorRef)) =>
      actorRef forward c
      stay()
    case Event(TaskFinish, x@WorkActor(actorRef))     =>
      context.stop(actorRef)
      log.info(s"work finish ${stateName}")
      nextStatus
  }

  implicit def actorRef2WorkActor(actorRef: ActorRef): WorkActor = WorkActor(actorRef)

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
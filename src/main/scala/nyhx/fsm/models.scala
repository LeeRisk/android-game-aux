package nyhx.fsm

import akka.actor.ActorRef


case object TaskFinish

case class TaskFailure(exception: Exception)


trait BaseStatus {
  def statusToString(s: AnyRef) = s.getClass.getName.replace("$", ".")

  override def toString: String = statusToString(this)
}

trait BaseData

object UnInit extends BaseData

object NoData extends BaseData

case class WorkActor(actorRef: ActorRef) extends BaseData

case class WorkActorList(actorRef: List[ActorRef]) extends BaseData


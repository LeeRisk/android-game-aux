package nyhx.fsm

import akka.actor.ActorRef


case object TaskFinish

case class TaskFailure(exception: Exception)


trait BaseStatus

trait ScenesStatus extends BaseStatus


object ScenesStatus {

  object Returns extends ScenesStatus

  object GotoRoom extends ScenesStatus

  object GotoWdj extends ScenesStatus

  object Failure extends ScenesStatus

  object Finish extends ScenesStatus

}


trait BaseData

object UnInit extends BaseData

object NoData extends BaseData

case class WorkActor(actorRef: ActorRef) extends BaseData

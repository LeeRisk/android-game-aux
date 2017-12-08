package nyhx.block

import scala.language.implicitConversions

import akka.actor.{Actor, ActorRef}
import models.{ClientRequest, Commands}
import nyhx.{Images, NoFindPicException, RecAction, Result}
import utensil.{FindPicBuild, IsFindPic, NoFindPic}


trait Action

object Action {
  implicit def rec2action(rec: RecAction): Rec = Rec(rec)

  implicit def sequ2action(sequence: Sequence): Sequ = Sequ(sequence)

  case class Rec(recAction: RecAction) extends Action

  case class Sequ(sequence: Sequence) extends Action

}

trait Patten

object Patten {

  case class Next(action: Action) extends Patten

  case class Util(action: RecAction, num: Int) extends Patten

  case class Repeat(action: Action, num: Int) extends Patten

}

case class Sequence(actions: Seq[Patten]) {

  def next(recAction: Action) = Sequence(Patten.Next(recAction) +: actions)

  def util(recAction: RecAction, maxNum: Int) = Sequence(Patten.Util(recAction, maxNum) +: actions)

  def repeat(action: Action, num: Int) = Sequence(Patten.Repeat(action, num) +: actions)

  val isEnd = actions.isEmpty

  def head = actions.head

  def tail = actions.tail
}

object Sequence {

  import Patten._

  def run(sequence: Sequence)(clientRequest: ClientRequest, sender: ActorRef): Sequence = {
    def execRecAction(recAction: RecAction) = recAction(clientRequest) match {
      case Result.Failure(x)   => throw x
      case Result.Execution(x) =>
        sender ! x
        Some(recAction)
      case Result.Success(x)   =>
        sender ! x
        None
    }

    def runByRec(action: RecAction) = {
      val result = execRecAction(action)
      result match {
        case Some(x) => Sequence(Patten.Next(x) +: sequence.tail)
        case None    => Sequence(sequence.tail)
      }
    }

    def runBySequence(sequ: Sequence) = {
      val result = run(sequ)(clientRequest, sender)
      if(result.isEnd)
        Sequence(sequence.tail)
      else
        Sequence(Patten.Next(result) +: sequence.tail)

    }

    val action = sequence.head
    action match {
      case Next(Action.Rec(action))         => runByRec(action)
      case Next(Action.Sequ(sequ))          => runBySequence(sequ)
      case Util(Action.Rec(recAction), 0)   => throw new Exception("")
      case Util(Action.Rec(recAction), num) => runByRec(recAction)
      case Repeat(action, 0)                => run(Sequence(sequence.tail))(clientRequest, sender)
      case Repeat(action, num)              => run(Sequence(Next(action) +: Repeat(action, num - 1) +: sequence.tail))(clientRequest, sender)
    }
  }
}

class WarActor extends Actor {
  var action = List(
    touchReturns,
    goToRoom,
    mustFind(findAdventure(_)),
    touchAdventure,

  )

  override def receive: Receive = {
    case c: ClientRequest =>
      action.head(c) match {
        case Result.Failure(x)   => throw x
        case Result.Execution(x) =>
          sender() ! x
        case Result.Success(x)   =>
          sender() ! x
          action = action.tail
      }
  }


  def touchReturns = RecAction { implicit clientRequest =>
    val result = findReturns.run()
    result match {
      case IsFindPic(point) => Result.Execution(Commands().addTap(point))
      case NoFindPic()      => Result.Success()
    }
  }

  def goToRoom = RecAction { implicit clientRequest =>
    if(findAdventure.run().isFind)
      Result.Success()
    else
      findReturnsRoom.run() match {
        case IsFindPic(point) => Result.Execution(Commands().addTap(point))
        case NoFindPic()      => Result.Success()
      }
  }


  def touchAdventure = RecAction { implicit clientRequest =>
    val result = findAdventure.run()
    result match {
      case IsFindPic(point) => Result.Success(Commands().addTap(point))
      case NoFindPic()      => Result.Failure(NoFindPicException("no find adventure in room"))
    }
  }

  def mustFind[T <: FindPicBuild.Request](f: ClientRequest => FindPicBuild[T]) = RecAction { implicit clientRequest =>
    val findPicBuild = f(clientRequest)
    findPicBuild.run() match {
      case IsFindPic(_) => Result.Success()
      case NoFindPic()  => Result.Failure(NoFindPicException(findPicBuild.goal.get.simpleName))
    }
  }

  def findReturns(implicit clientRequest: ClientRequest) =
    FindPicBuild()
      .withGoal(Images.returns.toGoal)
      .withOriginal(clientRequest.image.toOriginal)

  def findReturnsRoom(implicit clientRequest: ClientRequest) = FindPicBuild()
    .withGoal(Images.returns_room.toGoal)
    .withOriginal(clientRequest.image.toOriginal)

  def findAdventure(implicit clientRequest: ClientRequest) = FindPicBuild()
    .withGoal(Images.Adventure.adventure.toGoal)
    .withOriginal(clientRequest.image.toOriginal)

}

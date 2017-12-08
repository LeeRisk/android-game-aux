package nyhx.sequence

import scala.language.implicitConversions

import akka.actor.ActorRef
import models.ClientRequest
import nyhx.{RecAction, Result}

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
      case Next(Action.Rec(action)) => runByRec(action)
      case Next(Action.Sequ(sequ))  => runBySequence(sequ)

      case Util(recAction, 0)   => throw new Exception("")
      case Util(recAction, num) =>
        execRecAction(recAction) match {
          case Some(x) => Sequence(Patten.Util(x, num - 1) +: sequence.tail)
          case None    => Sequence(sequence.tail)
        }

      case Repeat(action, 0)   => run(Sequence(sequence.tail))(clientRequest, sender)
      case Repeat(action, num) => run(Sequence(Next(action) +: Repeat(action, num - 1) +: sequence.tail))(clientRequest, sender)
    }
  }
}

case class Sequence(actions: Seq[Patten] = Nil) {

  def next(recAction: Action) = Sequence(actions :+ Patten.Next(recAction))

  def util(recAction: RecAction, maxNum: Int) = Sequence(actions :+ Patten.Util(recAction, maxNum))

  def repeat(action: Action, num: Int) = Sequence(actions :+ Patten.Repeat(action, num))

  val isEnd = actions.isEmpty

  def head = actions.head

  def tail = actions.tail
}
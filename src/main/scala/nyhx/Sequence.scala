package nyhx

import akka.actor.ActorRef
import models.ClientRequest
import org.slf4j.LoggerFactory

case class Sequence(seq: Seq[Either[RecAction, Sequence]] = Seq()) {
  val logger = LoggerFactory.getLogger("sequence")

  def next(recAction: RecAction) = Sequence(seq :+ Left(recAction))

  def next(sequence: Sequence) = Sequence(seq :+ Right(sequence))

  def run(x: ClientRequest, sender: ActorRef): Sequence = {
    require(seq.nonEmpty)
    seq.head match {
      case Left(recAction) =>
        val result = recAction(x)
        result match {
          case Result.Become(f, commands) =>
            sender ! commands
            Sequence(Left(f) +: seq.tail)
          case Result.Failure(exception)  =>
            throw exception
          case Result.Execution(commands) =>
            sender ! commands
            this
          case Result.Success(commands)   =>
            sender ! commands
            Sequence(seq.tail)
        }
      case Right(sequence) =>
        val result = sequence.run(x, sender)
        if(result.isEnd)
          Sequence(seq.tail)
        else
          Sequence(Right(result) +: seq.tail)
    }
  }

  def isEnd = seq.isEmpty
}

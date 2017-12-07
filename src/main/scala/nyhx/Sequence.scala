package nyhx

import akka.actor.ActorRef
import models.ClientRequest
import org.slf4j.LoggerFactory

//trait Task
//
//object Task {
//  implicit def recAction2task(r: RecAction): RecActionTask = RecActionTask(r)
//
//  implicit def sequence2task(sequence: Sequence): SequenceTask = SequenceTask(sequence)
//}
//
//case class RecActionTask(recAction: RecAction) extends Task
//
//case class SequenceTask(sequence: Sequence) extends Task
//
//
//trait Instruction
//
//case class NextInstruction(task: Task) extends Instruction
//
//case class RepeatInstruction(n: Int, task: Task) extends Instruction
//
//
//case class Sequence(taskInstructions: Seq[Instruction] = Nil) {
//  val logger = LoggerFactory.getLogger("sequence")
//
//  def next(task: Task) = Sequence(taskInstructions :+ NextInstruction(task))
//
//  def ~>(task: Task): Sequence = next(task)
//
//  def repeat(n: Int, task: Task) = Sequence(taskInstructions :+ RepeatInstruction(n, task))
//
//  private def runRecAction(recAction: RecAction)(c: ClientRequest, sender: ActorRef): Option[RecAction] = recAction(c) match {
//    case Result.Become(f, commands) =>
//      sender ! commands
//      Some(f)
//    case Result.Failure(exception)  =>
//      throw exception
//    case Result.Execution(commands) =>
//      sender ! commands
//      None
//    case Result.Success(commands)   =>
//      sender ! commands
//      None
//  }
//
//  def isEnd = taskInstructions.isEmpty
//
//  def run(c: ClientRequest, sender: ActorRef): Sequence = {
//    require(taskInstructions.nonEmpty)
//    val instruction = taskInstructions.head
//
//    instruction match {
//      //next
//      case NextInstruction(RecActionTask(recAction)) =>
//        val become = runRecAction(recAction)(c, sender)
//        if(become.isEmpty)
//          Sequence(taskInstructions.tail)
//        else
//          Sequence(NextInstruction(become.get) +: taskInstructions.tail)
//      case NextInstruction(SequenceTask(sequence))   =>
//        val result = sequence.run(c, sender)
//        if(result.isEnd)
//          Sequence(taskInstructions.tail)
//        else
//          Sequence(NextInstruction(result) +: taskInstructions.tail)
//
//
//      //repeat
//      case RepeatInstruction(0, task)                     =>
//        Sequence(taskInstructions.tail).run(c, sender)
//      case RepeatInstruction(n, RecActionTask(recAction)) =>
//        val become = runRecAction(recAction)(c, sender)
//        if(become.isEmpty)
//          Sequence(RepeatInstruction(n - 1, RecActionTask(recAction)) +: taskInstructions.tail)
//        else
//          Sequence(NextInstruction(become.get) +: RepeatInstruction(n - 1, RecActionTask(recAction)) +: taskInstructions.tail)
//
//      case RepeatInstruction(n, SequenceTask(sequence)) =>
//        val result = sequence.run(c, sender)
//        if(result.isEnd)
//          Sequence(RepeatInstruction(n - 1, SequenceTask(sequence)) +: taskInstructions.tail)
//        else
//          Sequence(NextInstruction(result) +: RepeatInstruction(n - 1, SequenceTask(sequence)) +: taskInstructions.tail)
//
//
//    }
//  }
//}


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

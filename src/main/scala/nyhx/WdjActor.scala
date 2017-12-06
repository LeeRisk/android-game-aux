package nyhx

import akka.actor.{Actor, ActorRef}
import models._
import org.slf4j.LoggerFactory
import utensil.FindPic


case class Sequence(seq: Seq[Either[RecAction, Sequence]] = Seq()) {
  val logger = LoggerFactory.getLogger("sequence")

  def ~>(recAction: RecAction) = Sequence(seq :+ Left(recAction))

  def ~>(sequence: Sequence) = Sequence(seq :+ Right(sequence))

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

class WdjActor() extends Actor {
  val logger = LoggerFactory.getLogger("wjd")


  var sequence = (Sequence()
    ~> touchReturns
    ~> goToRoom
    ~> utilFind(10)(Images.Wdj.wuDouJi.toGoal)
    ~> findAndTouch(Images.Wdj.wuDouJi.toGoal)

    ~> utilFind(10)(Images.Wdj.shenShen.toGoal)
    ~> findAndTouch(Images.Wdj.shenShen.toGoal)

    ~> war
    ~> war
    ~> end
    )

  def war = (Sequence()
    ~> utilFind(10)(Images.Wdj.matchBattle.toGoal)
    ~> findAndTouch(Images.Wdj.matchBattle.toGoal)

    ~> utilFind(60)(Images.Wdj.fightResult.toGoal)
    ~> touchWarResult(Images.Wdj.fightResult.toGoal)
    )

  def end = RecAction { implicit e =>
    println("end")
    ???

  }

  def touchWarResult(goal: GoalImage) =
    FindPicAction("touch war result")
      .withIsFind(e => Result.Execution(Commands(TapCommand(0, 0))))
      .withNoFind(() => Result.Success())
      .withGoal(goal)
      .run()

  override def receive: Receive = {
    case e: ClientRequest =>
      sequence = sequence.run(e, sender())

  }

  def touchReturns: RecAction =
    FindPicAction("touch return")
      .withIsFind(e => Result.Execution(Commands().addTap(e).addDelay(1000)))
      .withNoFind(() => Result.Success(Commands()))
      .withGoal(Images.returns.toGoal)
      .run()

  def goToRoom: RecAction =
    FindPicAction("go to room")
      .withIsFind(e => Result.Execution(Commands().addTap(e).addDelay(1000)))
      .withNoFind(() => Result.Success(Commands()))
      .withGoal(Images.returns_room.toGoal)
      .run()

  def utilFind(maxNum: Int = 100)(image: GoalImage): RecAction =
    FindPicAction(s"util find ${image.simpleName}")
      .withIsFind(e => Result.Success())
      .withNoFind(() => Result.Become(utilFind(maxNum - 1)(image)))
      .withGoal(image)
      .run()


  def findAndTouch(goalImage: GoalImage) =
    FindPicAction("find and touch")
      .withIsFind(e => Result.Success(Commands().addTap(e)))
      .withNoFind(() => Result.Failure(new Exception(s"no find ${goalImage.simpleName}")))
      .withGoal(goalImage)
      .run()
}

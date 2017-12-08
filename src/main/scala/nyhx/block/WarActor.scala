package nyhx.block

import java.util.concurrent.LinkedBlockingDeque

import akka.actor.Actor
import models.{ClientRequest, Commands, Point}
import nyhx.{Images, NoFindPicException, RecAction, Result}
import utensil.{FindPicBuild, IsFindPic, NoFindPic}

class WarActor extends Actor {
  val x = new LinkedBlockingDeque[ClientRequest]()

  implicit def getNext: () => ClientRequest = () => x.pollFirst()

  var action = List(
    touchReturns,
    goToRoom,
    mustFind(findAdventure(_)),
    touchAdventure,

  )


  //  def goToArea(area:Point) = RecAction{
  //
  //  }

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

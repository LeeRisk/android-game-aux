package nyhx.sequence

import models.Commands
import nyhx.{NoFindPicException, RecAction, Result}
import org.slf4j.Logger
import utensil.{IsFindPic, NoFindPic}

trait Scenes {
  def logger: Logger


  def goToRoom = RecAction { implicit clientRequest =>
    if(Find.adventure.run().isFind)
      Result.Success()
    else
      Find.goToRoom.run() match {
        case IsFindPic(point) => Result.Execution(Commands().addTap(point))
        case NoFindPic()      => Result.Success()
      }
  }

  def touchDetermine = RecAction { implicit clientRequest =>
    val result = Find.returns.run()
    logger.info(s"find determine :${result.isFind}")
    result match {
      case IsFindPic(point) => Result.Execution(Commands().addTap(point))
      case NoFindPic()      => Result.Failure(NoFindPicException("no find determine"))
    }
  }

  def touchReturns = RecAction { implicit clientRequest =>
    val result = Find.returns.run()
    logger.info(s"find return :${result.isFind}")
    result match {
      case IsFindPic(point) => Result.Execution(Commands().addTap(point))
      case NoFindPic()      => Result.Success()
    }
  }

  def touchStart = RecAction { implicit c =>
    val result = Find.start.run()
    logger.info(s"find start :${result.isFind}")
    result match {
      case IsFindPic(point) => Result.Success(Commands().addTap(point))
      case NoFindPic()      => Result.Failure(NoFindPicException("no find start"))
    }
  }

  def touchAdventure = RecAction { implicit clientRequest =>
    val result = Find.adventure.run()
    logger.info(s"find adventure in room :${result.isFind}")
    result match {
      case IsFindPic(point) => Result.Success(Commands().addTap(point))
      case NoFindPic()      => Result.Failure(NoFindPicException("no find adventure in room"))
    }
  }

}

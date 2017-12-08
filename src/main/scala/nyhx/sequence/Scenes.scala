package nyhx.sequence

import models.Commands
import nyhx.{RecAction, Result}
import org.slf4j.Logger
import utensil.{IsFindPic, NoFindPic}

trait Scenes {
  def logger: Logger


  def goToRoom = RecAction { implicit clientRequest =>
    if(Find.adventure(clientRequest).run().isFind)
      Result.Success()
    else
      Find.goToRoom(clientRequest).run() match {
        case IsFindPic(point) => Result.Execution(Commands().addTap(point))
        case NoFindPic()      => Result.Success()
      }
  }

  def touchReturns = RecAction { implicit clientRequest =>
    val result = Find.returns(clientRequest).run()
    logger.info(s"find return :${result.isFind}")
    result match {
      case IsFindPic(point) => Result.Execution(Commands().addTap(point))
      case NoFindPic()      => Result.Success()
    }
  }

}

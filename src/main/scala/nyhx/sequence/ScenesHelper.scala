package nyhx.sequence


import models._
import org.slf4j.Logger
import utensil.{IsFindPic, NoFindPic}


trait ScenesHelper {
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

  def goToGruen = RecAction { implicit clientRequest =>
    if(Find.goToRoom(clientRequest).run().isFind)
      Result.Success()
    else
      Find.goToGakuen(clientRequest).run() match {
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
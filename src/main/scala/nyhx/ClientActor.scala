package nyhx

import akka.actor.{Actor, ActorRef, Props}
import models.{ClientRequest, Commands}
import nyhx.squence.WarSixFourActor
import org.slf4j.LoggerFactory

sealed trait Result {
  def commands: Commands
}

object Result {

  trait Complete extends Result

  trait Continue extends Result

  case class Success(commands: Commands = Commands()) extends Complete

  case class Failure(exception: Exception) extends Complete {
    override def commands: Commands = throw exception
  }

  case class Execution(commands: Commands) extends Continue

  case class Become(f: RecAction, commands: Commands = Commands()) extends Continue

}

trait RecAction extends (ClientRequest => Result)

object RecAction {
  def apply(f: ClientRequest => Result): RecAction = (v1: ClientRequest) => f(v1)
}

case class EmEmptyException() extends Exception("em empty")

case class NoFindPicException(s: String) extends Exception(s)

class ClientActor() extends Actor {
  val logger         = LoggerFactory.getLogger("client-actor")
  var work: ActorRef = context.system.actorOf(Props(new WarSixFourActor()))


  override def receive = {
    case x@ClientRequest(screen) =>
      logger.debug(s"receive screen file :${x.image.name}")
      work.forward(x)
  }


}


package nyhx

import akka.actor.{Actor, ActorRef, Props}
import models.ClientRequest
import nyhx.sequence.WarActor
import org.slf4j.LoggerFactory


class ClientActor() extends Actor {
  val logger         = LoggerFactory.getLogger("client-actor")
  var work: ActorRef = context.system.actorOf(Props(new WarActor()))


  override def receive = {
    case x@ClientRequest(screen) =>
      logger.debug(s"receive screen file :${x.image.name}")
      work.forward(x)
  }


}


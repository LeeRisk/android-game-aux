package nyhx

import akka.actor.{Actor, Props}
import akka.testkit.TestActorRef
import models.{DismissedTaskFinish, WarTaskEnd}
import org.scalatest.{FunSuite, WordSpec}
import sources.{AkkaSources, ImageSources}

class ClientActorTest extends WordSpec with AkkaSources with ImageSources {
  lazy val actor = TestActorRef[ClientActor](Props(new ClientActor))

  def nothingActor() = actorSystem.actorOf(Props(new Actor {
    override def receive: Receive = {
      case e =>
    }
  }))

  "war" in {
    assert(actor.underlyingActor.stateName === ClientActor.War)
    actor ! WarTaskEnd(nothingActor())
    assert(actor.underlyingActor.stateName === ClientActor.Dismissed)
  }
  "dismissed" in {

    actor ! DismissedTaskFinish(nothingActor())
    assert(actor.underlyingActor.stateName === ClientActor.War)
  }

}

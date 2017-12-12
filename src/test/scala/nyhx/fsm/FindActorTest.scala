package nyhx.fsm

import akka.testkit.{TestActorRef, TestProbe}
import models.{ClientRequest, Commands, TapCommand}
import nyhx.{Find, Images}
import org.scalatest._
import sources.{AkkaSources, ImageSources}

class FindActorTest extends WordSpec with ImageSources with AkkaSources with Matchers {

  import testkit._

  object original {
    val adventure = readOriginal("adventure.png")
  }

  "touch" in {
    val parent = TestProbe()
    val actor = parent.childActorOf(FindActor.touch(Find(Images.returns)))

    val c = ClientRequest(original.adventure)
    actor.tell(c, testActor)
    expectMsgPF() {
      case x: Commands =>
        assert(x.seq.length === 1)
        x.seq.head shouldBe a[TapCommand]
    }
    parent.expectMsg(TaskFinish)

  }
}

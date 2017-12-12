package sources

import akka.actor.ActorSystem
import akka.testkit.TestKit
import models.{GoalImage, OriginalImage}
import org.scalatest.{BeforeAndAfterAll, Suite}

trait AkkaSources extends BeforeAndAfterAll {
  this: Suite =>
  implicit lazy val actorSystem = ActorSystem("test")
  lazy          val testkit     = new TestKit(actorSystem) with akka.testkit.ImplicitSender
  implicit lazy val exec        = actorSystem.dispatcher

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    actorSystem
    testkit
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    actorSystem.terminate()
  }

}

trait ImageSources {
  val userDir = System.getProperty("user.dir").replaceAll("\\\\", "/")

  def readOriginal(name: String) = OriginalImage(s"$userDir/images-original/$name")

  def readGoal(name: String) = GoalImage(s"$userDir/images-goal/$name")
}

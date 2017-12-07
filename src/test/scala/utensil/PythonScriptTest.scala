package utensil

import scala.concurrent.Await
import scala.concurrent.duration.Duration.Inf

import org.scalatest.WordSpec
import utensil.PythonScript._

class PythonScriptTest extends WordSpec {
  "jep" in {
    assert(Await.result(PythonScript.eval(_.getValue("1+1").asInstanceOf[java.lang.Long]), Inf) == 2)
  }
  "find image" in {
    val userDir = System.getProperty("user.dir").replaceAll("\\\\", "/")
    println(userDir)
    val result = findPic(
      s"$userDir/src/test/resources/screen-gruan.png",
      s"$userDir/src/test/resources/wu_dou_ji.png",
      "default")
    println(result)
    assert(result._1 > 0.97)
  }
}

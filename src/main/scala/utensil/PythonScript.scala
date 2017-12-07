package utensil

import java.util.concurrent.Executors

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

import jep.Jep

object PythonScript {
  var libpath = System.getProperty("java.library.path")
  val userDir = System.getProperty("user.dir")
  libpath = s"${userDir}\\python-script;${libpath}"
  System.setProperty("java.library.path", libpath)
  println(System.getProperty("java.library.path"))
  implicit val exec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))


  private val jep = Future {
    val jep = new Jep(false)
    jep.runScript("./python-script/find_pic.py")
    jep
  }

  def eval[T](f: Jep => T): Future[T] = jep.map(f)


  def findPic(original: String, goal: String, patten: String): (Double, Int, Int) = {
    val regex = "\\(([0-9|.]+), ?([0-9]+), ?([0-9]+)\\)".r
    val result = jep.map(jep =>
      jep.getValue(s"jvm_find_pic('$original','$goal','$patten')") match {
        case regex(sim, x, y) => (sim.toDouble, x.toInt, y.toInt)
      }
    )

    Await.result(result, Duration.Inf)
  }
}



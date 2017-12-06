package utensil

import models.{GoalImage, OriginalImage, Point}
import utensil.PythonScript.findPic


case class FindPic(original: OriginalImage, goal: GoalImage, threshold: Double = 0.95) {
  lazy val (similarity, topLeftPoint) = {
    val (max, x, y) = findPic(
      original.name.replaceAll("\\\\", "/"),
      goal.name.replaceAll("\\\\", "/"))
    max -> Point(x, y)
  }

  def point: Option[Point] = if(isFind) Some(topLeftPoint) else None

  def isFind: Boolean = similarity >= threshold

  def noFind: Boolean = !isFind
}


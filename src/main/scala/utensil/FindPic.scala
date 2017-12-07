package utensil

import models.{GoalImage, OriginalImage, Point}
import utensil.PythonScript.findPic


case class FindPic(original: OriginalImage,
                   goal: GoalImage,
                   threshold: Double=0.95,
                   patten: FindPic.Patten.Value = FindPic.Patten.Default) {
  lazy val (similarity, topLeftPoint) = {
    val (max, x, y) = {
      val originalName = original.name.replaceAll("\\\\", "/")
      val goalName = goal.name.replaceAll("\\\\", "/")
      findPic(originalName, goalName, patten.toString)
    }
    max -> Point(x, y)
  }

//  def result = if (isFind) IsFindPic(point.get) else NoFindPic()

  def point: Option[Point] = if(isFind) Some(topLeftPoint) else None

  def isFind: Boolean = similarity >= threshold

  def noFind: Boolean = !isFind
}
//trait FindPicResult
//case class IsFindPic(point: Point) extends FindPicResult
//case class NoFindPic() extends FindPicResult


object FindPic {

  object Patten extends Enumeration {
    val Default = Value("default")
    val Edge    = Value("edge")
  }


}
package nyhx.sequence

import models.{ClientRequest, Commands, GoalImage}
import nyhx.{Images, NoFindPicException, RecAction, Result}
import org.slf4j.Logger
import utensil.{FindPicBuild, IsFindPic, NoFindPic}

object Find {
  def returns(implicit clientRequest: ClientRequest) = find(Images.returns.toGoal)

  def goToRoom(implicit clientRequest: ClientRequest) = find(Images.returns_room.toGoal)

  def adventure(implicit clientRequest: ClientRequest) = find(Images.Adventure.adventure.toGoal)

  def grouping(implicit clientRequest: ClientRequest) = find(Images.Adventure.grouping.toGoal)

  def start(implicit clientRequest: ClientRequest) = find(Images.start.toGoal)

  def totalTurn(implicit clientRequest: ClientRequest) = find(Images.Adventure.totalTurn.toGoal)

  def mpEmpty(implicit clientRequest: ClientRequest) = find(Images.Adventure.mpEmpty.toGoal)

  def navigateCondition(implicit clientRequest: ClientRequest) = find(Images.Adventure.navigateCondition.toGoal)
  def find(image: GoalImage)(implicit clientRequest: ClientRequest) = FindPicBuild()
    .withGoal(image.toGoal)
    .withOriginal(clientRequest.image.toOriginal)

}


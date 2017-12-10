package nyhx

import better.files.File

object Images {
  val returns        = image("returns.png")
  val returns_gakuen = image("returns-gakuen.png")
  val returns_room   = image("returns-room.png")
  val start          = image("start.png")
  val determine      = image("determine.png")

  object YuanZiWu {
    val yuanZiWu           = image("yzw.png")
    val dismissed          = image("yzw-dismissed.png")
    val selectStudent      = image("yzw-select-student.png")
    val dismissedDetermine = image("yzw-dismissed-determine.png")

    val dismissedSelectStudentDetermine = image("yzw-dismissed-select-student-determine.png")
  }

  val lv1 = image("lv1.png")

  object Retrieve {
    val retrieve = image("retrieve.png")
    val an       = image("retrieve-an.png")
    val shui     = image("retrieve-shui.png")
  }


  object Area {
    val one   = image("area-one.png")
    val two   = image("area-two.png")
    val three = image("area-three.png")
    val four  = image("area-four.png")
    val five  = image("area-five.png")
    val six   = image("area-six.png")
  }

  object Adventure {
    val needSurvey = image("need-survey.png")

    val start             = Images.start
    val adventure         = image("adventure.png")
    val grouping          = image("adventure-grouping.png")
    val totalTurn         = image("adventure-total-turn.png")
    val mpEmpty           = image("adventure-mp-empty.png")
    val navigateCondition = image("adventure-navigate-condition.png")
    val selectA           = image("select-a.png")
  }

  object Wdj {
    val wuDouJi     = image("wdj.png")
    val shenShen    = image("wdj-sen_shen.png")
    val matchBattle = image("wdj-match-battle.png")
    val fightResult = image("wdj-fight-result.png")
  }

  //  val gakuen    = image("xue-yuang.png")
  //  val room      = image("fang-jiang.png")
  //  val returns   = image("fanghui.png")
  //  val closeX    = image("X")
  //  val determine = image("determine")
  //  val start     = image("Attachment:start.png|Attachment:start-1.png")
  //
  //  val icon_huPoZiQuan = image("icon-hpzq.png")
  //
  //
  //  object Treatment {
  //    val selectTreatment     = image("选择治疗1")
  //    val resourceComsumption = image("Attachment:zi-yuan-xiao-hao.png")
  //  }
  //
  //  object WuDouJi {
  //    val wuDouJi       = image("武斗祭.png")
  //    val shenShen      = image("武斗祭-神圣")
  //    val piPeiZhanDou  = image("武斗祭-匹配战斗.png")
  //    val zhanDouJieGoo = image("武斗祭-战斗结果.png")
  //  }
  //
  //  object Adventure {
  //    val adventure = image("adventure.png")
  //    val start     = ImagePaths.start
  //    val bianZu    = image("adventure-grouping.png")
  //    val node      = image("节点.png")
  //    val selectA   = image("选择A.png")
  //
  //    val zongHuiHe = image("adventure-total-turn.png")
  //    val mpEmpty   = image("adventure-mp-empty.png")
  //  }
  //
  //  object Explore {
  //    val explore    = image("explore")
  //    val complete   = image("explore-complete")
  //    val settlement = image("explore-settlement")
  //
  //    val earnReward    = image("earn-reward")
  //    val exitAdventrue = image("explore-exit-adventrue")
  //
  //  }

  def image(s: String) = models.Image(File(s"images-goal/$s").pathAsString)
}

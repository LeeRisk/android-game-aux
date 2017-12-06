package nyhx

import models.Point

object Points {

  object Treatment {
    val huPoZiQuan = Point(304, 164, "huPoZiQuan")
  }

  object Adventure {

    object Area {
      val one   = Point(176, 76, "1")
      val two   = Point(285, 74, "2")
      val three = Point(391, 74, "3")
      val four  = Point(509, 73, "4")
      val five  = Point(626, 75, "5")
      val six   = Point(742, 77, "6")
    }

    val next = Point(914, 306, "next")
  }

  object Explore {
    val getPrize = Point(788, 448)

    val Area = Adventure.Area

    object Node {
      val one   = Point(229, 441, "1")
      val two   = Point(480, 439, "2")
      val three = Point(753, 434, "3")
    }

  }

  object Group {
    val a = Point(42, 144, "a")
    val b = Point(42, 197, "b")
    val c = Point(42, 262, "c")
    val d = Point(42, 325, "d")
    val e = Point(42, 385, "e")
  }

}

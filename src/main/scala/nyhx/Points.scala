package nyhx

import models.Point

object Points {

  object Treatment {
    val huPoZiQuan = Point(304, 164, "huPoZiQuan")
  }

  object Area {
    val one   = Point(176, 76, "1")
    val two   = Point(285, 74, "2")
    val three = Point(391, 74, "3")
    val four  = Point(509, 73, "4")
    val five  = Point(626, 75, "5")
    val six   = Point(742, 77, "6")
  }

  object Adventure {

    object AreaSix {

      object One {
        val b = Point(353, 194, "b")
      }

    }

    object AreaThree {

      object One {
        val b    = Point(373, 203, "b")
        val e    = Point(207, 173, "e")
        val f    = Point(606, 172, "f")
        val boss = Point(863, 155, "boss")
      }

    }

    object Five {

      object One {
        val b = Point(263, 192, "b")
        val c = Point(137, 396, "g")
        val d = Point(307, 416, "d")
        val e = Point(480, 421, "e")
        val f = Point(680, 438, "f")
      }

    }

    object Three {

      object Six {
        val b = Point(131, 242)
        val c = Point(99, 437)
        val g = Point(362, 463)
        val h = Point(599, 441)
      }

    }

    object Two {

      object Six {
        val b = Point(185, 237)
        val c = Point(123, 393)
        val e = Point(330, 369)
        val f = Point(438, 483)
      }

    }

    val next = Point(914, 306, "next")
  }

  object Explore {
    val getPrize = Point(788, 448)


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

package edu.porg

import org.scalatest.FunSuite

import java.util.concurrent
import scala.collection.mutable.ArrayBuffer


object TestMutex {
  var count = 0
  val result: ArrayBuffer[Int] = ArrayBuffer[Int]()
  def add1() = {
    result.synchronized {
      count = count + 1
      val c = count
      result.append(c)
    }
  }


  def add2() = {
    result.synchronized {
      count = count + 1
      val c = count
      result.append(c)
    }
  }
}

class PorgTest extends FunSuite{

  test("aa") {
    val t1 = new Thread(()=> (1 to 10000).foreach(x => TestMutex.add1()))
    val t2 = new Thread(()=> (1 to 10000).foreach(x => TestMutex.add2()))
    t1.start()
    t2.start()

    t1.join()
    t2.join()

    val result = TestMutex.result.foldLeft((true, 0))({
      case (valid, num) =>
        if (!valid._1)
          (false, 0)
        else {
          if (valid._2 < num)
            (true, num)
          else
            (false, 0)
        }
    })

    assert(result._1)
  }
}

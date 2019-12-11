package edu.porg

import org.scalatest.FunSuite
import java.util.concurrent

import edu.porg.message.{JobInfo, TaskInfo}
import net.liftweb.json._

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

  test("test_json") {
    implicit val format = DefaultFormats
    val inputString = "{\"status\":\"success\",\"message\":\"\",\"action\":\"finish task\",\"data\":{\"job\":{\"name\":\"flatMapExample\",\"jid\":1,\"jtype\":\"maponly\",\"host\":\"http://219.228.148.81:4049/upload\",\"dir\":\"flatMapJob/output\",\"program\":\"http://219.228.148.81:4049/download?url=flatMapJob/flatMapExample.js\",\"map_num\":4,\"reduce_num\":0},\"tType\":\"maponly\",\"tid\":0,\"tArg\":{\"input\":[\"http://219.228.148.81:4049/download?url=flatMapJob/input/part-1\"],\"output\":\"flatMapJob/output/0\",\"cache\":[]}}}"
    val parsed = parse(inputString)
    val status = (parsed \ "status").extract[String]
    val message = (parsed \ "message").extract[String]
    val action = (parsed \ "action").extract[String]
    val data = (parsed \ "data").extract[TaskInfo]
    println(data)

  }
}
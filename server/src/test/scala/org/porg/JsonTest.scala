package org.porg

import org.scalatest.FunSuite
import net.liftweb.json._
import org.porg.Jobs.JobInfo
import org.porg.Tasks.{TaskArgInfo, TaskInfo}

class JsonTest extends FunSuite{

  test("aa") {
    case class P(a1: String, b1: Int)
    val json = parse(
      """
        |{ "a1" : {"a":1, "b":2}, "b1": 2}
      """.stripMargin)
    println(json)
    val p = P("x", 1)
    val js = json.values.asInstanceOf[Map[String, _]]
    println(js)
//    json.
  }

  test("job info") {
    val jobInfo = JobInfo("jobname", 13, "MapOnly", "job_dir", "program", 8, 1)
    val jobjson = jobInfo.toJson()

    val js = parse(jobjson)
    implicit val formats = DefaultFormats
    assert(jobInfo.toString == js.extract[JobInfo].toString)
  }

  test("task arg") {
    val taskarg = TaskArgInfo(Seq("input1", "input2"), "output", Seq("cache1"))
    println(taskarg.toJson())
    val taskarg2 = TaskArgInfo(Seq("input1", "input2"), "output", Nil)
    println(taskarg2.toJson())
  }

  test("task info") {
    val taskarg2 = TaskArgInfo(Seq("input1", "input2"), "output", Nil)
    val jobInfo = JobInfo("jobname", 13, "MapOnly", "job_dir", "program", 8, 1)
    val taskInfo = TaskInfo(jobInfo, "maponly", 3, taskarg2)

    val j = taskInfo.toJson()
    println(j)

    val json = parse(j)
    val jv = json.values.asInstanceOf[Map[String, _]]
    println(jv)
    implicit val formats = DefaultFormats
    val t = (json \ "tType").extract[String]

    println(t)

  }
}

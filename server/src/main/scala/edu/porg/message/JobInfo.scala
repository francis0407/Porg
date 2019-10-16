package edu.porg.message

case class JobInfo(
  name: String,
  jid: Int,
  jtype: String,
  dir: String,
  program: String,
  map_num: Int,
  reduce_num: Int) {
  def toJson(): String = {
    s"""
       |{
       |  "name": "$name",
       |  "jid": $jid,
       |  "jtype": "$jtype",
       |  "dir": "$dir",
       |  "program": "$program"
       |  "map_num": $map_num,
       |  "reduce_num": $reduce_num
       |}
   """.stripMargin
  }
}

case class NewJobInfo(
  name: String,
  jtype: String,
  dir: String,
  program: String,
  inputs: Seq[String],
  reduce_num: Int)


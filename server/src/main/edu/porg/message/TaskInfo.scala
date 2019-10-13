package edu.porg.message

// Format:
// {
//   "job":
//   {
//     "name": jobname String,
//     "jid": jobId Int,
//     "type": ["maponly", "mapcache", "mapreduce"] String,
//     "dir": jobDir String,
//     "program": programDir String,
//     "map_num": mapNumber Int,
//     "reduce_num": mapNumber Int
//   },
//   "type": ["maponly", "mapcache", "mapshuffle" "reduce"],
//   "tid": tid Int,
//   "task_arg": taskInfo  TaskInfo
// }

case class TaskArgInfo(input: Seq[String], output: String, cache: Seq[String]) {
  def toJson(): String = {
    s"""
       |{
       |   "input": [${input.map(s => '"' + s + '"').mkString(",")}],
       |   "output": "$output",
       |   "cache": [${cache.map(s => '"' + s + '"').mkString(",")}]
       |}
    """.stripMargin
  }
}

case class TaskInfo(jobInfo: JobInfo, tType: String, tid: Int, tArg: TaskArgInfo) {
  def toJson(): String = {
    s"""
       |{
       |  "job": ${jobInfo.toJson()},
       |  "tType": "$tType",
       |  "tid": $tid,
       |  "tArg": ${tArg.toJson()}
       |}
     """.stripMargin
  }
}

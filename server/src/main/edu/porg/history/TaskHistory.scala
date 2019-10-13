package edu.porg.history

object TaskHistory {

}

case class MapOnlyTaskHistory(
  startTime: Long,
  finishTime: Long,
  output: String
)
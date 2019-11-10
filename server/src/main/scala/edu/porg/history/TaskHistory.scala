package edu.porg.history

import edu.porg.scheduler.TaskID

abstract class TaskHistory

object TaskHistory {

}

case class MapOnlyTaskHistory(
  taskID: TaskID,
  uniqueID: Int,
  startTime: Long,
  finishTime: Long,
  input: String,
  output: String
) extends TaskHistory
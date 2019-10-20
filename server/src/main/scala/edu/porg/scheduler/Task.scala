package edu.porg.scheduler

import edu.porg.message.{TaskArgInfo, TaskInfo}
import edu.porg.util._

abstract class Task(val job: Job, val tid: Int) extends Logging {

  val taskID = TaskID(job.jid, tid)

  val uniqueID = Task.newId()

  var startTime: Long = 0
  var finishTime: Long = 0

  def output: String

  def isFinishMark: Boolean = false

  def genTaskInfo(): TaskInfo
}

object Task extends AutoIncreased

class MapOnlyTask(job: Job, tid: Int, input: String) extends Task(job, tid) {
  override def output: String = s"${job.dir}/$tid"

  override def genTaskInfo(): TaskInfo =
    TaskInfo(job.genJobInfo(), "maponly", tid, TaskArgInfo(Seq(input), output, Nil))
}


class FinishMarkTask(job: Job) extends Task(job, -1) {
  override def genTaskInfo(): TaskInfo = null

  override def isFinishMark: Boolean = true

  override def output: String = ""
}

object TaskID {
  def apply(job: Job, task: Task): TaskID = {
    TaskID(job.jid, task.tid)
  }
}

case class TaskID(jid: Int, tid: Int) {
  override def toString: String = s"job$jid-task$tid"
}


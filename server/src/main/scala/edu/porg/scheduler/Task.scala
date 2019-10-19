package edu.porg.scheduler

import edu.porg.message.TaskInfo
import edu.porg.util._

abstract class Task(val job: Job, val tid: Int) extends Logging {

  val taskID = TaskID(job.jid, tid)

  val uniqueID = Task.newId()

  def isFinishMark: Boolean = false

  def genTaskInfo(): TaskInfo
}

object Task extends AutoIncreased

class MapOnlyTask(job: Job, tid: Int, input: String) extends Task(job, tid) {

}


object TaskID {
  def apply(job: Job, task: Task): TaskID = {
    TaskID(job.jid, task.tid)
  }
}

case class TaskID(jid: Int, tid: Int) {
  override def toString: String = s"job$jid-task$tid"
}


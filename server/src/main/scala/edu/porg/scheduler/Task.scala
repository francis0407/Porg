package edu.porg.scheduler

import edu.porg.util._

abstract class Task(val tid: Int) extends Logging {

  val uniqueID = Task.newId()

  def isFinishMark: Boolean = false
}

object Task extends AutoIncreased

class MapOnlyTask(tid: Int, input: String) extends Task(tid) {

}


object TaskID {
  def apply(job: Job, task: Task): TaskID = {
    TaskID(job.jid, task.tid)
  }
}

case class TaskID(jid: Int, tid: Int) {
  override def toString: String = s"job$jid-task$tid"
}


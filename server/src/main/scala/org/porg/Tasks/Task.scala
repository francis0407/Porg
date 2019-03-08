package org.porg.Tasks

import org.porg.Jobs.Job
import org.porg.Util.Logging
import org.porg.Worker

case class TaskId (jid: Int, tid: Int) {
  override def toString: String = s"job$jid-task$tid"
}

abstract class Task(job: Job, tid: Int) extends Logging{

  val taskId = TaskId(job.jid, tid)

  var finished: Boolean = false

  var waiting: Boolean = true

  private var info: TaskInfo = null

  def finish(taskInfo: TaskInfo) = {
    info = taskInfo
  }

  def consume() = {
    waiting = false
  }

  def reset() = {
    waiting = true
  }
}

case class MapCacheTask(
    job: Job,
    tid: Int,
    input: String) extends Task(job, tid) {


  def genTasks(start: Int): Seq[MapCacheTask] = {
    Nil
  }
}

case class MapSortTask(
    job: Job,
    tid: Int,
    input: String,
    output_num: Int) extends Task(job, tid) {

}
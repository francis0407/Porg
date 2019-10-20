package edu.porg.scheduler

import java.util.concurrent.ConcurrentHashMap

import edu.porg.history.{JobHistory, MapOnlyJobHistory}
import edu.porg.message.JobInfo
import edu.porg.util._

abstract class Job(
    val name: String,
    val uid: String,
    val host: String,
    val dir: String,
    val program: String,
    val inputs: Seq[String]) extends Logging {

  val jid: Int = Job.newId()

  def getName: String = name

  def jobType: String

  def prepareToSchedule(): Unit

  def isFinished: Boolean

  def getNextTaskScheduler(lastScheduler: TaskScheduler): TaskScheduler

  def genJobInfo(): JobInfo

  def finishJob(): Unit
}

object Job extends AutoIncreased

class MapOnlyJob(
    name: String,
    uid: String,
    host: String,
    dir: String,
    program: String,
    inputs: Seq[String])
  extends Job(name, uid, host, dir, program, inputs) {

  override def jobType: String = "maponly"

  override def prepareToSchedule(): Unit = {
    MapOnlyJobHistory.newJobHistory(this)
  }

  override def isFinished: Boolean = {
    if (taskScheduler == null)
      false
    else
      taskScheduler.isFinished
  }

  private var taskScheduler: MapOnlyTaskScheduler = null

  override def getNextTaskScheduler(lastScheduler: TaskScheduler): TaskScheduler = {
    if (taskScheduler != null)
      null
    else {
      val tasks = inputs.zipWithIndex.map({case (input: String, tid: Int) => new MapOnlyTask(this, tid, input)})
      taskScheduler = new MapOnlyTaskScheduler(this, tasks)
      taskScheduler
    }
  }

  override def genJobInfo(): JobInfo = JobInfo(name, jid, jobType, host, dir, program, inputs.size, 0)

  override def finishJob(): Unit = JobHistory.finishJob(this)
}

//class MapCacheJob(
//    name: String,
//    uid: String,
//    dir: String,
//    program: String,
//    inputs: Seq[String])
//  extends Job(name, uid, dir, program, inputs) {
//  override def prepareToSchedule(): Unit = ???
//
//  override def isFinished: Boolean = ???
//
//  override def getNextTaskScheduler(lastScheduler: TaskScheduler): TaskScheduler = ???
//}

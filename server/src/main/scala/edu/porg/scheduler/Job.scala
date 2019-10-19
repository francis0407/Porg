package edu.porg.scheduler

import java.util.concurrent.ConcurrentHashMap

import edu.porg.history.MapOnlyJobHistory
import edu.porg.util._

abstract class Job(
    name: String,
    uid: String,
    dir: String,
    program: String,
    inputs: Seq[String]) extends Logging {

  val jid: Int = Job.newId()

  def getName: String = name


  def prepareToSchedule(): Unit

  def isFinished: Boolean

  def getNextTaskScheduler(lastScheduler: TaskScheduler): TaskScheduler
}

object Job extends AutoIncreased

class MapOnlyJob(
    name: String,
    uid: String,
    dir: String,
    program: String,
    inputs: Seq[String])
  extends Job(name, uid, dir, program, inputs) {
  override def prepareToSchedule(): Unit = ???

  override def isFinished: Boolean = ???

  private var hasGeneratedScheduler = false

  override def getNextTaskScheduler(lastScheduler: TaskScheduler): TaskScheduler = {
    if (hasGeneratedScheduler)
      null
    else {
      val tasks = inputs.zipWithIndex.map({case (input: String, tid: Int) => new MapOnlyTask(this, tid, input)})
      hasGeneratedScheduler = true
      new MapOnlyTaskScheduler(this, tasks)
    }
  }
}

class MapCacheJob(
    name: String,
    uid: String,
    dir: String,
    program: String,
    inputs: Seq[String])
  extends Job(name, uid, dir, program, inputs) {
  override def prepareToSchedule(): Unit = ???

  override def isFinished: Boolean = ???

  override def getNextTaskScheduler(lastScheduler: TaskScheduler): TaskScheduler = ???
}

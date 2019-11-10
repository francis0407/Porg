package edu.porg.history


import java.util.concurrent.ConcurrentSkipListMap
import java.util.Date
import edu.porg.scheduler.{Job, MapOnlyJob}
import edu.porg.util.Logging

import scala.collection.mutable

abstract class JobHistory(
    val name: String,
    val jid: Int) {

}

object JobHistory {
  protected[history] val history: ConcurrentSkipListMap[Int, JobHistory] = new ConcurrentSkipListMap[Int, JobHistory]()

  var currentJobID: Int = -1
  var finishedJobNumber: Int = 0
  def registerNewJobHistory(job: Job): Unit = {
    job match {
      case moj: MapOnlyJob =>
        MapOnlyJobHistory.newJobHistory(moj)
    }
  }

  def finishJob(job: Job): Unit = {
    this.synchronized {
      currentJobID = -1
      finishedJobNumber += 1

    }
    history.get(job.jid) match {
      case moj: MapOnlyJobHistory =>
        moj.finishTime = new Date()
    }
  }

  def getFinishedJobsNumber: Int = this.synchronized {
    finishedJobNumber
  }

  // JobName, TaskNumber, RunningTaskNumber, FinishedTaskNumber
  def getCurrentJobInfo: (String, Int, Int, Int) = this.synchronized {
    if (currentJobID == -1)
      ("No Running Job", 0, 0, 0)
    else {
      val job = history.get(currentJobID)
      job match {
        case moj: MapOnlyJobHistory =>
          (moj.name, moj.inputs.size, moj.doing_num, moj.finish_tasks.size)
        case null =>
          ("", -1, -1, -1)
      }
    }
  }

  def getTaskFinishTime(jid: Int): Seq[Long] = {
    val jobHistory = if (jid == 0) {
      this.synchronized {
        if (currentJobID == -1)
          null
        else
          history.get(currentJobID)
      }
    } else {
      history.get(jid)
    }
    jobHistory match {
      case moj: MapOnlyJobHistory =>
        moj.finish_tasks.map(x => x.finishTime - x.startTime)
      case null =>
        Seq()
    }
  }

  def getJobList: Array[Int] = {
    history.keySet().toArray().map(_.asInstanceOf[Int])
  }

  def getJobInfo(jid: Int): JobHistory = {
    history.get(jid) match {
      case h: MapOnlyJobHistory =>
        h
      case _ =>
        null
    }
  }
}


class NoSuchJobHistory() extends JobHistory("No Such Job", 0)


class MapOnlyJobHistory(
    name: String,
    jid: Int,
    val dir: String,
    val program: String,
    val inputs: Seq[String],
    val startTime: Date,
    var finishTime: Date,
    var finish_tasks: Seq[MapOnlyTaskHistory],
    var doing_num: Int,
    var redo_num: Int) extends JobHistory(name, jid)

object MapOnlyJobHistory extends Logging {

  def newJobHistory(job: MapOnlyJob): Unit = {
    JobHistory.history.put(job.jid, new MapOnlyJobHistory(
      job.name,
      job.jid,
      job.dir,
      job.program,
      job.inputs,
      new Date(),
      null,
      Seq(),
      0,
      0
    ))
    JobHistory.currentJobID = job.jid
  }

  def finishTask(jid: Int, task: MapOnlyTaskHistory): Unit = {
    JobHistory.history.get(jid) match {
      case h: MapOnlyJobHistory => h.synchronized {
        h.finish_tasks = h.finish_tasks :+ task
        h.doing_num = h.doing_num - 1
      }
      case null =>
        logger.error("History not found.")
    }
  }

  def failTask(jid: Int): Unit = {
    JobHistory.history.get(jid) match {
      case h: MapOnlyJobHistory => h.synchronized {
        h.doing_num = h.doing_num - 1
        h.redo_num = h.redo_num + 1
      }
      case null =>
        logger.error("History not found.")
    }
  }

  def doTask(jid: Int): Unit = {
    JobHistory.history.get(jid) match {
      case h: MapOnlyJobHistory =>
        h.synchronized {
          h.doing_num = h.doing_num + 1
        }
      case null =>
        logger.error("History not found.")
    }
  }
}

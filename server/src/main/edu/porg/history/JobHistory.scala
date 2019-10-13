package edu.porg.history


import java.util.concurrent.ConcurrentSkipListMap

import edu.porg.scheduler.{Job, MapOnlyJob}
import edu.porg.util.Logging

import scala.collection.mutable

abstract class JobHistory {

}

object JobHistory {
  protected[history] val history: ConcurrentSkipListMap[Int, JobHistory] = new ConcurrentSkipListMap[Int, JobHistory]()

  def registerNewJobHistory(job: Job): Unit = {
    job match {
      case moj: MapOnlyJob =>
        MapOnlyJobHistory.newJobHistory(moj)
    }
  }

}

case class MapOnlyJobHistory(
    name: String,
    jid: Int,
    dir: String,
    program: String,
    inputs: Seq[String],
    var finish_tasks: Seq[MapOnlyTaskHistory],
    var doing_num: Int,
    var redo_num: Int) extends JobHistory

object MapOnlyJobHistory extends Logging {

  def newJobHistory(job: MapOnlyJob): Unit = {
    JobHistory.history.put(job.jid, MapOnlyJobHistory(
      job.name,
      job.jid,
      job.dir,
      job.program,
      job.inputs,
      Seq(),
      0,
      0
    ))
  }

  def finishTask(jid: Int, task: MapOnlyTaskHistory): Unit = {
    JobHistory.history.get(jid) match {
      case h: MapOnlyJobHistory =>
        h.finish_tasks = h.finish_tasks :+ task
        h.doing_num = h.doing_num - 1
      case null =>
        logger.error("History not found.")
    }
  }

  def failTask(jid: Int): Unit = {
    JobHistory.history.get(jid) match {
      case h: MapOnlyJobHistory =>
        h.doing_num = h.doing_num - 1
        h.redo_num = h.redo_num + 1
      case null =>
        logger.error("History not found.")
    }
  }

  def doTask(jid: Int): Unit = {
    JobHistory.history.get(jid) match {
      case h: MapOnlyJobHistory =>
        h.doing_num = h.doing_num + 1
      case null =>
        logger.error("History not found.")
    }
  }

}

package edu.porg.history


import edu.porg.scheduler.MapOnlyJob
import edu.porg.util.Logging

import scala.collection.mutable



case class MapOnlyJobHistory(
    name: String,
    jid: Int,
    dir: String,
    program: String,
    inputs: Seq[String],
    var finish_tasks: Seq[MapOnlyTaskHistory],
    var doing_num: Int,
    var redo_num: Int)

object MapOnlyJobHistory extends Logging {

  private val history: mutable.Map[Int, MapOnlyJobHistory] = mutable.Map[Int, MapOnlyJobHistory]()

  def newJobHistory(job: MapOnlyJob): Unit = {
    history.put(job.jid, MapOnlyJobHistory(
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
    history.get(jid) match {
      case Some(h: MapOnlyJobHistory) =>
        h.finish_tasks = h.finish_tasks :+ task
        h.doing_num = h.doing_num - 1
      case None =>
        logger.error("History not found.")
    }
  }

  def failTask(jid: Int): Unit = {
    history.get(jid) match {
      case Some(h: MapOnlyJobHistory) =>
        h.doing_num = h.doing_num - 1
        h.redo_num = h.redo_num + 1
      case None =>
        logger.error("History not found.")
    }
  }

  def doTask(jid: Int): Unit = {
    history.get(jid) match {
      case Some(h: MapOnlyJobHistory) =>
        h.doing_num = h.doing_num + 1
      case None =>
        logger.error("History not found.")
    }
  }

}

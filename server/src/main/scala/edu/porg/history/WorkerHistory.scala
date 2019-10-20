package edu.porg.history

import java.util.concurrent.ConcurrentSkipListMap
import java.util.Date

import scala.collection.mutable.MutableList
import edu.porg.scheduler._

import scala.collection.mutable

abstract class WorkerHistory {

}

object WorkerHistory {
  protected[history] val history: ConcurrentSkipListMap[String, WorkerHistory] = new ConcurrentSkipListMap[String, WorkerHistory]()

  protected[history] val workerNumberHistory: mutable.ListBuffer[(Date, Long)] = mutable.ListBuffer[(Date, Long)]()

  def beginWorkerNumberSampleThread(duration: Long, maxRecords: Int): Unit = {
    new Thread(()=> {
      while (true) {
        workerNumberHistory.synchronized {
          if (workerNumberHistory.size == maxRecords) {
            workerNumberHistory.remove(0)
          }
          val newEle = (new Date(), WorkerManager.getConnectedWorkersNumber.toLong)
          workerNumberHistory += newEle
        }
        Thread.sleep(duration)
      }
    }).start()
  }

  def getWorkerNumberHistory: List[(Date, Long)]= {
    workerNumberHistory.synchronized {
      workerNumberHistory.toList
    }
  }

  def registerNewWorker(worker: Worker): Unit = {
    worker match {
      case p: PorgWorker =>
        history.put(worker.ID, PorgWorkerHistory(worker.ID, worker.uniqueID, WorkerStatus.Waiting.toString, Seq()))
      case p: TestWorker =>
        history.put(worker.ID, PorgWorkerHistory(worker.ID, worker.uniqueID, WorkerStatus.Waiting.toString, Seq()))
    }
  }

  def updateStatus(workerID: String, workerStatus: WorkerStatus.WorkerStatus): Unit = {
    history.get(workerID) match {
      case p: PorgWorkerHistory =>
        p.status = workerStatus.toString
    }
  }

  def finishTask(workerID: String, jobID: Int, taskHistory: TaskHistory): Unit = {
    history.get(workerID) match {
      case p: PorgWorkerHistory =>
        p.finishedTasks = p.finishedTasks :+ TaskHistoryWithJobID(jobID, taskHistory)
    }
  }

}

case class TaskHistoryWithJobID(jobID: Int, taskHistory: TaskHistory)

case class PorgWorkerHistory(
    workerID: String,
    uniqueID: Int,
    var status: String,
    var finishedTasks: Seq[TaskHistoryWithJobID]
) extends WorkerHistory
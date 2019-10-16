package edu.porg.history

import java.util.concurrent.ConcurrentSkipListMap

import edu.porg.scheduler.{PorgWorker, TestWorker, Worker, WorkerStatus}

abstract class WorkerHistory {

}

object WorkerHistory {
  protected[history] val history: ConcurrentSkipListMap[String, WorkerHistory] = new ConcurrentSkipListMap[String, WorkerHistory]()

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
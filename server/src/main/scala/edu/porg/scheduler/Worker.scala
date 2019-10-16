package edu.porg.scheduler

import java.util.concurrent.{BlockingQueue, ConcurrentHashMap}

import edu.porg.history.WorkerHistory
import edu.porg.util.{AutoIncreased, Logging}
import org.java_websocket.WebSocket

import scala.sys.process.processInternal.LinkedBlockingQueue


object WorkerStatus extends Enumeration {
  type WorkerStatus = Value
  val Working = Value("Working")
  val Waiting = Value("Waiting")
  val Disconnect = Value("Disconnect")
}

abstract class Worker {

  def ID: String

  def disconnect(): Unit

  def executeTask(task: Task): Unit

  val uniqueID: Int = Worker.newId()

  var status = WorkerStatus.Waiting
}

object Worker extends AutoIncreased with Logging

object PorgWorker {
  def getWorkerID(webSocket: WebSocket): String =
    webSocket.getResourceDescriptor
}

class PorgWorker(webSocket: WebSocket) extends Worker {

  override def ID: String = PorgWorker.getWorkerID(webSocket)

  override def disconnect(): Unit = {
    status = WorkerStatus.Disconnect
    WorkerHistory.updateStatus(ID, status)
  }

}

class TestWorker(id: String) extends Worker {

  override def ID: String = id

  override def disconnect(): Unit = {}

}

object WorkerManager extends Logging {
  private val workers: ConcurrentHashMap[String, Worker] = new ConcurrentHashMap[String, Worker]()

  private val idealWorkers: BlockingQueue[Worker] = new LinkedBlockingQueue[Worker]()

  def registerWorker(worker: Worker): Unit = {
    logger.info(s"New worker ${worker.ID}")
    workers.put(worker.ID, worker)
    idealWorkers.put(worker)
    WorkerHistory.registerNewWorker(worker)
  }

  def disconnect(workerID: String): Unit = {
    logger.info(s"Worker disconnect: $workerID")
    val worker = workers.remove(workerID)
    if (worker != null) {
      worker.disconnect()
    } else {
      // TODO: disconnect may be called before registerWorker, add a list and check the workers
    }
  }

  def getIdealWorker(): Worker = {
    var result: Worker = null
    while(result == null) {
      val worker = idealWorkers.take()
      worker.status match {
        case WorkerStatus.Waiting =>
          result = worker
        case WorkerStatus.Working =>
          logger.error(s"Worker ${worker.ID} in IdealWorkers is working, better to disconnect.")
      }
    }
    result
  }
}
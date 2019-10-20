package edu.porg.scheduler

import java.util.concurrent.{BlockingQueue, ConcurrentHashMap, LinkedBlockingQueue}
import java.util.Date

import edu.porg.history.WorkerHistory
import edu.porg.message.BasicMessage
import edu.porg.util.{AutoIncreased, Logging}
import org.java_websocket.WebSocket
import net.liftweb.json._


object WorkerStatus extends Enumeration {
  type WorkerStatus = Value
  val Working = Value("Working")
  val Waiting = Value("Waiting")
  val Disconnect = Value("Disconnect")
}

abstract class Worker extends Logging {

  def ID: String

  def disconnect(): Unit

  def executeTask(task: Task): Unit

  val uniqueID: Int = Worker.newId()

  var status = WorkerStatus.Waiting

  var workingTask: Task = null
}

object Worker extends AutoIncreased

object PorgWorker {
  def getWorkerID(webSocket: WebSocket): String =
    webSocket.getRemoteSocketAddress.toString
}

class PorgWorker(webSocket: WebSocket) extends Worker {

  override def ID: String = PorgWorker.getWorkerID(webSocket)

  override def disconnect(): Unit = {
    status = WorkerStatus.Disconnect
    WorkerHistory.updateStatus(ID, status)
  }

  override def executeTask(task: Task): Unit = {
    val taskInfo = task.genTaskInfo()
    val msg = BasicMessage(taskInfo)
    val prettyMsg = prettyRender(parse(msg.toJson()))
    //    println(prettymsg)
    logger.info(s"send new task ${task.taskID.toString} to $ID")
    webSocket.send(prettyMsg)

    task.startTime = new Date().getTime
    status = WorkerStatus.Working
    workingTask = task
    WorkerHistory.updateStatus(ID, status)
  }

}

class TestWorker(id: String) extends Worker {

  override def ID: String = id

  override def disconnect(): Unit = {}

  override def executeTask(task: Task): Unit = ???

}

object WorkerManager extends Logging {
  private val workers: ConcurrentHashMap[String, Worker] = new ConcurrentHashMap[String, Worker]()

  private val idealWorkers: BlockingQueue[Worker] = new LinkedBlockingQueue[Worker]()

  private var idealWorkersNumber: Int = 0

  def getConnectedWorkersNumber: Int = workers.size()

  def getIdealWorkersNumber: Int = idealWorkersNumber


  def finishTask(workerID: String): Unit = {
    val worker = workers.get(workerID)
    if (worker != null) {
      worker.status = WorkerStatus.Waiting
      idealWorkers.add(worker)
      idealWorkersNumber += 1
    }
  }

  def registerWorker(worker: Worker): Unit = {
    logger.info(s"New worker ${worker.ID}")
    workers.put(worker.ID, worker)
    idealWorkers.put(worker)
    idealWorkersNumber += 1
    WorkerHistory.registerNewWorker(worker)
  }

  def disconnect(workerID: String): Task = {
    val worker = workers.remove(workerID)
    if (worker != null) {
      logger.info(s"Worker disconnect: $workerID")
      if (worker.status == WorkerStatus.Waiting)
        idealWorkersNumber -= 1
      worker.disconnect()
      worker.workingTask
    } else {
      null
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
          idealWorkersNumber -= 1
        case WorkerStatus.Working =>
          result = null
          logger.error(s"Worker ${worker.ID} in IdealWorkers is working, better to disconnect.")
        case WorkerStatus.Disconnect =>
          result = null
      }
    }
    result
  }
}
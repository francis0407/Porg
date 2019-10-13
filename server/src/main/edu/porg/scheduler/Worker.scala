package edu.porg.scheduler

import edu.porg.util.{AutoIncreased, Logging}
import org.java_websocket.WebSocket


object WorkerStatus extends Enumeration {
  type WorkerStatus = Value
  val Working = Value("Working")
  val Waiting = Value("Waiting")
}

abstract class Worker {

  def ID: String

  val uniqueID: Int = Worker.newId()

  var status = WorkerStatus.Waiting
}

object Worker extends AutoIncreased with Logging

object PorgWorker {
  def getWorkerID(webSocket: WebSocket): String =
    webSocket.getResourceDescriptor
}

class PorgWorker(websocket: WebSocket) extends Worker {

  override def ID: String = PorgWorker.getWorkerID(websocket)

}

class TestWorker(id: String) extends Worker {

  override def ID: String = id
}
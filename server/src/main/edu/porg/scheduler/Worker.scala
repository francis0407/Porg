package edu.porg.scheduler

import org.java_websocket.WebSocket

abstract class Worker {
  def ID: String
}


object PorgWorker {
  def getWorkerID(webSocket: WebSocket): String =
    webSocket.getResourceDescriptor
}

class PorgWorker(websocket: WebSocket) extends Worker {

  override def ID: String = PorgWorker.getWorkerID(websocket)

}
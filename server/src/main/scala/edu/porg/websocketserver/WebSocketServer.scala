package edu.porg.websocketserver

import java.net.InetSocketAddress

import edu.porg.PorgConf
import edu.porg.message._
import edu.porg.util.Logging
import edu.porg.scheduler._

import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import net.liftweb.json._


class PorgServer(port: Int, porgConf: PorgConf, scheduler: JobScheduler) extends WebSocketServer(new InetSocketAddress(port)) with Logging {

  implicit val format = DefaultFormats

  override def onOpen(webSocket: WebSocket, clientHandshake: ClientHandshake): Unit = {

  }

  override def onClose(webSocket: WebSocket, i: Int, s: String, b: Boolean): Unit = {
//    val worker = Worker.workers.get(webSocket)
//    if (worker != null) {
//      logger.info(s"Worker disconnected: ${webSocket.getResourceDescriptor}")
//      worker.diconnect()
//      Worker.workers.remove(webSocket)
//    }
    scheduler.disconnect(PorgWorker.getWorkerID(webSocket))
  }

  override def onMessage(webSocket: WebSocket, s: String): Unit = {
    try {
      val parsed = parse(s)
      val status = (parsed \ "status").extract[String]
      val message = (parsed \ "message").extract[String]
      val action = (parsed \ "action").extract[String]
      action match {
        case "register job" =>
          val newJobInfo = (parsed \ "data").extract[NewJobInfo]
          val job: Job = newJobInfo.jtype.toLowerCase match {
            case "maponly" =>
              MapOnlyJob(
                newJobInfo.name,
                webSocket.getResourceDescriptor,
                newJobInfo.dir,
                newJobInfo.program,
                newJobInfo.inputs
              )
            case "mapcache" =>
              MapCacheJob(
                newJobInfo.name,
                webSocket.getResourceDescriptor,
                newJobInfo.dir,
                newJobInfo.program,
                newJobInfo.inputs
              )
            case _ =>
              throw new Exception("not support")
          }
          scheduler.registerJob(job)

        case "register worker" =>
//          val worker = new PorgWorker(webSocket, scheduler)
          val worker = new PorgWorker(webSocket)
          scheduler.registerWorker(worker)
          logger.info(s"New worker from: ${webSocket.getResourceDescriptor}")

        case "finish task" =>
          val taskInfo = (parsed \ "data").extract[TaskInfo]
          scheduler.finishTask(TaskID(taskInfo.jobInfo.jid, taskInfo.tid), taskInfo)
        // Finish a task
      }

    } catch {
      case e =>
        logger.error(s"message type error: $s")
        e.printStackTrace()
    }
  }

  override def onError(webSocket: WebSocket, e: Exception): Unit = {
    logger.error(
      s"error from server: ${webSocket.getResourceDescriptor}, ${e.getMessage}")
  }

  override def onStart(): Unit = {
    logger.info("Start Porg WebSocketServer...")
  }
}
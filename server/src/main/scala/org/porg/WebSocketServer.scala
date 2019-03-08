package org.porg

import java.net.InetSocketAddress

import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import net.liftweb.json._
import org.porg.Jobs.{Job, MapCacheJob, MapOnlyJob, NewJobInfo}
import org.porg.Tasks.{TaskId, TaskInfo}
import org.porg.Util.Logging


class PorgServer(port: Int, porgConf: PorgConf, scheduler: Scheduler) extends WebSocketServer(new InetSocketAddress(port)) with Logging{

  implicit val format = DefaultFormats

  override def onOpen(webSocket: WebSocket, clientHandshake: ClientHandshake): Unit = {

  }

  override def onClose(webSocket: WebSocket, i: Int, s: String, b: Boolean): Unit = {
    val worker = Worker.workers.get(webSocket)
    if (worker != null) {
      logger.info(s"Worker disconnected: ${webSocket.getResourceDescriptor}")
      worker.diconnect()
      Worker.workers.remove(webSocket)
    }
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
          val worker = new PorgWorker(webSocket, scheduler)
          scheduler.registerWorker(worker)
          logger.info(s"New worker from: ${webSocket.getResourceDescriptor}")

        case "finish task" =>
          val taskInfo = (parsed \ "data").extract[TaskInfo]
          scheduler.finishTask(TaskId(taskInfo.jobInfo.jid, taskInfo.tid), taskInfo)
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
    logger.info("Start Porg Server...")
  }
}
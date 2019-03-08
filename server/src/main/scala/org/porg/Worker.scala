package org.porg

import java.util.concurrent.ConcurrentHashMap

import org.java_websocket.WebSocket
import org.porg.Tasks.{Task, TaskInfo}
import org.porg.Util.AutoIncreased

abstract class Worker(scheduler: Scheduler) {

  def executeTask(task: Task): Unit

  def isAvailable(): Boolean = avaliable

  def diconnect(): Unit

  var avaliable = true
}

object Worker {

  val workers: ConcurrentHashMap[WebSocket, Worker] = new ConcurrentHashMap[WebSocket, Worker]()

}

case class TestWorker(scheduler: Scheduler) extends Worker(scheduler) with AutoIncreased {

  val id = newId()

  override def toString: String = s"TestWorker$id"

  override def executeTask(task: Task): Unit = {
    val thread = new Thread(() =>{
      Thread.sleep(2000)
      val rand = scala.util.Random.nextInt(2)
//      if (rand == 0)
//        scheduler.finishTask(task.taskId, new TaskInfo())
//      else
//        scheduler.resetTask(task.taskId)
      scheduler.registerWorker(this)})
    thread.start()
  }

  override def diconnect(): Unit = { avaliable = false }
}

case class PorgWorker(webSocket: WebSocket, scheduler: Scheduler)
  extends Worker(scheduler) {

  require(!Worker.workers.contains(webSocket))

  Worker.workers.put(webSocket, this)

  override def executeTask(task: Task): Unit = {

  }

  override def diconnect(): Unit = {
    avaliable = false
  }

}

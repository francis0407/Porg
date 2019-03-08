package org.porg.Jobs

import java.util
import java.util.Collections

import org.porg.Util.{AutoIncreased, Logging}
import org.porg.Tasks._
import java.util.concurrent.{BlockingQueue, ConcurrentHashMap, LinkedBlockingQueue}

import scala.collection.JavaConverters._

abstract class Job(
    val name: String,
    val uid: String,
    val dir: String,
    val program: String,
    val inputs: Seq[String]) extends Logging{

  val jid: Int = Job.newId()

  val map_num: Int = inputs.size

  def finish(): Unit = synchronized {
    // TODO: finish job
    if (finished) return

    finished = true
    logger.info(s"Finish Job $name")
  }

  def prepare(): Unit = {
    // initialize mapcache tasks
    val taskWithId = inputs.zipWithIndex.map({
    case (input, idx) =>
    val mapTask = MapCacheTask(this, idx, input)
    (mapTask.taskId, mapTask)
  }).toMap
    tasks.putAll(taskWithId.asJava)

    taskWithId.foreach(x => waiting_tasks.offer(x._2)) // add waiting_tasks
  }

  def hasTask: Boolean = waiting_tasks.size > 0

  def isFinish: Boolean = {
    logger.debug(s"$finished, $finish_count, ${tasks.size()}")
    finished || finish_count == tasks.size()
  }

  def isAvailable: Boolean = !(finished || canceled)

  def consumeTask(): Task =  {
    val task = waiting_tasks.poll()
    task.consume()
    task
  }

  def resetTask(taskId: TaskId): Unit = this.synchronized {
    val task = tasks.get(taskId)
    if (task != null) {
      if (!task.waiting && !task.finished) {
        task.reset()
        waiting_tasks.offer(task)
      } else {
        logger.warn(s"Can't reset task: $taskId is waiting or has finished")
      }
    } else {
      logger.warn(s"Can't reset task: $taskId undefined")
    }
    this.notifyAll()
  }

  def finishTask(taskId: TaskId, taskInfo: TaskInfo): Unit = {
    this.synchronized {
      val task = tasks.get(taskId)
      if (task != null) {
        if (!task.finished)
          task.finish(taskInfo)
        finish_count = finish_count + 1
      } else {
        logger.warn(s"Can't finish task: $taskId undefined")
      }
      this.notifyAll()
    }
  }

  def waitForFinishOrFail(): Unit

  val tasks: util.Map[TaskId, Task] = new ConcurrentHashMap[TaskId, Task]()

  val waiting_tasks: BlockingQueue[Task] = new LinkedBlockingQueue[Task]()

  var finish_count: Int = 0
  protected var finished = false
  protected var canceled = false

}

object Job extends AutoIncreased


case class MapOnlyJob(
    _name: String,
    _uid: String,
    _dir: String,
    _program: String,
    _inputs: Seq[String])
  extends Job(_name, _uid, _dir, _program, _inputs) {

  override def waitForFinishOrFail(): Unit = this.synchronized {
    while(!(isFinish || waiting_tasks.size() > 0 || canceled)) {
      this.wait()
    }
    if (isFinish) {
      finish()
    }
  }
}

case class MapCacheJob(
    _name: String,
    _uid: String,
    _dir: String,
    _program: String,
    _inputs: Seq[String])
  extends Job(_name, _uid, _dir, _program, _inputs) {

  override def waitForFinishOrFail(): Unit = this.synchronized {
    while(!(isFinish || waiting_tasks.size() > 0 || canceled)) {
      this.wait()
    }
  }

  override def finishTask(taskId: TaskId, taskInfo: TaskInfo): Unit = this.synchronized {
    val task = tasks.get(taskId).asInstanceOf[MapCacheTask]
    if (task != null) {
      if (!task.finished) {
        task.finish(taskInfo)
        val newTasks = task.genTasks(tasks.size()) // Ad new tasks
        val tasksWithId = newTasks.map(t => (t.taskId, t))
        tasks.putAll(tasksWithId.toMap.asJava)
        tasksWithId.foreach(x => waiting_tasks.offer(x._2))
      }
    } else {
      logger.warn(s"Can't finish task: $taskId undefined")
    }
  }


}
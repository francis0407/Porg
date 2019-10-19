package edu.porg.scheduler

import java.util.concurrent.{BlockingQueue, ConcurrentHashMap, ConcurrentLinkedQueue, LinkedBlockingQueue}

import edu.porg.history.{MapOnlyTaskHistory, TaskHistory, WorkerHistory}
import edu.porg.message.TaskInfo
import edu.porg.util.Logging

abstract class TaskScheduler(job: Job) extends Logging{
  def run(): Unit

  def finishTask(workerID: String, taskID: TaskID, taskInfo: TaskInfo)

  def disconnect(workerID: String): Unit

  def isFinished: Boolean
}

class MapOnlyTaskScheduler(job: Job, tasks: Seq[MapOnlyTask]) extends TaskScheduler(job) {

  val waitingTaskQueue: BlockingQueue[Task] = new LinkedBlockingQueue[Task]()

  val runningTaskMap: ConcurrentHashMap[String, Task] = new ConcurrentHashMap[String, Task]()

  val finishedTaskQueue: ConcurrentLinkedQueue[Task] = new ConcurrentLinkedQueue[Task]()

  override def disconnect(workerID: String): Unit = ???

  override def finishTask(workerID: String, taskID: TaskID, taskInfo: TaskInfo): Unit = {
    this.synchronized {
      val task = runningTaskMap.get(taskID.toString)
      if (task == null) {
        logger.warn(s"Task ${taskID.toString} is not running")
      } else {
        runningTaskMap.remove(taskID.toString)
        finishedTaskQueue.add(task)
        // TODO: record task time
        val taskHistory = MapOnlyTaskHistory
        WorkerHistory.finishTask()
      }
    }
  }

  override def isFinished: Boolean = {
    finishedTaskQueue.size() == tasks.size
  }

  override def run(): Unit = {
    tasks.foreach(waitingTaskQueue.add)
    while(!isFinished) {
      val task = waitingTaskQueue.take()
      if (!task.isFinishMark) {
        val worker = WorkerManager.getIdealWorker()
        runningTaskMap.put(task.taskID.toString, task)
        worker.executeTask(task)
      }
    }
  }
}
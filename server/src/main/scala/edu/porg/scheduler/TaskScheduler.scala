package edu.porg.scheduler

import java.util.concurrent.{BlockingQueue, ConcurrentHashMap, ConcurrentLinkedQueue, LinkedBlockingQueue}
import java.util.Date

import edu.porg.history._
import edu.porg.message.TaskInfo
import edu.porg.util.Logging

abstract class TaskScheduler(job: Job) extends Logging{
  def run(): Unit

  def finishTask(workerID: String, taskID: TaskID, taskInfo: TaskInfo)

  def failTask(taskID: TaskID): Unit

  def isFinished: Boolean
}

class MapOnlyTaskScheduler(job: Job, tasks: Seq[MapOnlyTask]) extends TaskScheduler(job) {

  val waitingTaskQueue: BlockingQueue[Task] = new LinkedBlockingQueue[Task]()

  val runningTaskMap: ConcurrentHashMap[String, Task] = new ConcurrentHashMap[String, Task]()

  val finishedTaskQueue: ConcurrentLinkedQueue[Task] = new ConcurrentLinkedQueue[Task]()

  override def failTask(taskID: TaskID): Unit = {
    this.synchronized {
      val task = runningTaskMap.get(taskID.toString)
      if (task != null) {
        runningTaskMap.remove(taskID.toString)
        waitingTaskQueue.add(task)

        MapOnlyJobHistory.failTask(job.jid)
      }
    }
  }

  override def finishTask(workerID: String, taskID: TaskID, taskInfo: TaskInfo): Unit = {
    this.synchronized {
      val task = runningTaskMap.get(taskID.toString)
      if (task == null) {
        logger.warn(s"Task ${taskID.toString} is not running")
      } else {
        runningTaskMap.remove(taskID.toString)
        finishedTaskQueue.add(task)
        WorkerManager.finishTask(workerID)

        task.finishTime = new Date().getTime
        val taskHistory =
          MapOnlyTaskHistory(taskID, task.uniqueID, task.startTime, task.finishTime, taskInfo.tArg.input(0), taskInfo.tArg.output)
        WorkerHistory.finishTask(workerID, job.jid, taskHistory)
        MapOnlyJobHistory.finishTask(job.jid, taskHistory)

        if (isFinished) {
          waitingTaskQueue.add(new FinishMarkTask(job))
        }
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
        MapOnlyJobHistory.doTask(job.jid)
        worker.executeTask(task)
      }
    }
  }
}
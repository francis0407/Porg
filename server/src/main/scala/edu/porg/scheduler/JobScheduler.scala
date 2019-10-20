package edu.porg.scheduler

import java.util.concurrent.{BlockingQueue, ConcurrentHashMap, LinkedBlockingQueue}

import edu.porg.PorgConf
import edu.porg.history.{JobHistory, WorkerHistory}
import edu.porg.message.TaskInfo
import edu.porg.util.Logging

abstract class JobScheduler {

  def registerJob(job: Job): Unit

  def registerWorker(worker: Worker): Unit

  def finishTask(workerID: String, taskID: TaskID, taskInfo: TaskInfo)

  def disconnect(workerID: String): Unit

  def cancelJob(jobID: Int): Unit

  def run(): Unit
}

class PorgScheduler(porgConf: PorgConf) extends JobScheduler with Logging {

  val jobQueue: BlockingQueue[Job] = new LinkedBlockingQueue[Job]()

  val workers: ConcurrentHashMap[String, Worker] = new ConcurrentHashMap[String, Worker]()

  var taskScheduler: TaskScheduler = null

  var runningJob: Job = null

  override def registerJob(job: Job): Unit = {
    jobQueue.put(job)
    JobHistory.registerNewJobHistory(job)
    logger.info(s"New Job: ${job.getName}")
  }

  override def registerWorker(worker: Worker): Unit = {
    WorkerManager.registerWorker(worker)
  }

  override def finishTask(workerID: String, taskID: TaskID, taskInfo: TaskInfo): Unit = {
    logger.info(s"Finish task ${taskID.toString}")
    this.synchronized {
      if (taskScheduler != null) {
        taskScheduler.finishTask(workerID, taskID, taskInfo)
      }
    }
  }

  override def disconnect(workerID: String): Unit = {
    val runningTask = WorkerManager.disconnect(workerID)
    this.synchronized {
      if (runningTask != null && taskScheduler != null) {
        taskScheduler.failTask(runningTask.taskID)
      }
    }
  }

  override def cancelJob(jobID: Int): Unit = ???

  override def run(): Unit = {
    logger.info("Start Porg JobScheduler")
    while (true) {
      // 1. If there is no job, pick a job.

      if (runningJob == null) {
        // blocking take the first job from the job queue
        val newJob = jobQueue.take()
        newJob.prepareToSchedule()
        logger.info(s"Begin to schedule Job: #${newJob.jid}/${newJob.getName}")
        this.synchronized {
          runningJob = newJob
        }
      }
      // 2. schedule the job until it is finished
      while (!runningJob.isFinished) {
        this.synchronized {
          taskScheduler = runningJob.getNextTaskScheduler(taskScheduler)
        }
        taskScheduler.run()
      }

      runningJob.finishJob()
      this.synchronized {
        taskScheduler = null
        runningJob = null
      }
    }
  }
}

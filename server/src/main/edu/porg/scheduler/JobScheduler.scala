package edu.porg.scheduler

import java.util.concurrent.{BlockingQueue, ConcurrentHashMap, LinkedBlockingQueue}

import edu.porg.PorgConf
import edu.porg.history.{JobHistory, WorkerHistory}
import edu.porg.message.TaskInfo
import edu.porg.util.Logging

abstract class JobScheduler {

  def registerJob(job: Job): Unit

  def registerWorker(worker: Worker): Unit

  def finishTask(taskID: TaskID, taskInfo: TaskInfo)

  def disconnect(workerID: String): Unit

  def cancelJob(jobID: Int): Unit

  def run(): Unit
}

class PorgScheduler(porgConf: PorgConf) extends JobScheduler with Logging {

  val jobQueue: BlockingQueue[Job] = new LinkedBlockingQueue[Job](porgConf.getMaxWaitingJobs)

  val workers: ConcurrentHashMap[String, Worker] = new ConcurrentHashMap[String, Worker]()

  var taskScheduler: TaskScheduler = null

  var runningJob: Job = null

  override def registerJob(job: Job): Unit = {
    logger.info(s"New Job: ${job.getName}")
    jobQueue.put(job)
    JobHistory.registerNewJobHistory(job)
  }

  override def registerWorker(worker: Worker): Unit = {
    logger.info(s"New Worker: ${worker.ID}")
    workers.put(worker.ID, worker)
    WorkerHistory.registerNewWorker(worker)
  }

  override def finishTask(taskID: TaskID, taskInfo: TaskInfo): Unit = ???

  override def disconnect(workerID: String): Unit = ???

  override def cancelJob(jobID: Int): Unit = ???

  override def run(): Unit = {
    logger.info("Start Porg JobScheduler")
    while (true) {
      // 1. If there is no job is scheduling, pick a job.
      if (runningJob == null) {
        // blocking take the first job from the job queue
        runningJob = jobQueue.take()

      }
    }
  }
}

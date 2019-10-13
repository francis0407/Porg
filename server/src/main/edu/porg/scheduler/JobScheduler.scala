package edu.porg.scheduler

import java.util.concurrent.{BlockingQueue, LinkedBlockingQueue}

import edu.porg.PorgConf
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

  override def registerJob(job: Job): Unit = {
    logger.info(s"New Job: ${job.getName}")
    jobQueue.put(job)
    job.init
  }

  override def registerWorker(worker: Worker): Unit = ???

  override def finishTask(taskID: TaskID, taskInfo: TaskInfo): Unit = ???

  override def disconnect(workerID: String): Unit = ???

  override def cancelJob(jobID: Int): Unit = ???

  override def run(): Unit = ???
}

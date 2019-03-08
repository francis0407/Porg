package org.porg

import org.porg.Jobs.Job
import java.util.concurrent.{BlockingQueue, LinkedBlockingQueue}

import org.porg.Tasks.{TaskId, TaskInfo}
import org.porg.Util.Logging

class Scheduler(porgConf: PorgConf) extends Logging{

  private val jobs: BlockingQueue[Job] = new LinkedBlockingQueue[Job]()
  private var running_job: Option[Job] = None
  private def getWaitingJob(): Job = {
    val job = jobs.take()
    if (job.isAvailable)
      job
    else
      getWaitingJob()
  }

  private val workers: BlockingQueue[Worker] = new LinkedBlockingQueue[Worker]()
  private def getIdleWorker(): Worker = {
    val worker = workers.take()
    if (worker.isAvailable)
      worker
    else
      getIdleWorker()
  }

  def run() = {
    logger.info("Starting scheduler")
    while (true) {
      val job: Job = getWaitingJob() // blocking pop
      running_job = Some(job)
      job.prepare()

      while(job.hasTask || !job.isFinish) {
        // job has unfinished tasks
        if (job.hasTask) {
          val task = job.consumeTask() // blocking pop
          val worker = getIdleWorker() // blocking pop
          worker.executeTask(task) // send message
        } else {
          // TODO: redo task
          logger.info("???")
          job.waitForFinishOrFail()
        }
      }
    }
  }

  def registerWorker(worker: Worker) = {
    logger.info(s"New worker: ${worker.toString}")
    workers.put(worker)
  }

  def registerJob(job: Job) = {
    logger.info(s"New job: ${job.name}")
    jobs.put(job)
  }

  def finishTask(taskId: TaskId, taskInfo: TaskInfo): Unit = {
    logger.info(s"Finish task: ${taskId.toString}")
    running_job.get.finishTask(taskId, taskInfo)
  }

  def resetTask(taskId: TaskId): Unit = {
    logger.info(s"Reset task: ${taskId.toString}")
    running_job.get.resetTask(taskId)
  }
}

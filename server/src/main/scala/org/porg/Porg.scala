package org.porg

object Porg {

  def main(args: Array[String]): Unit = {
    val conf = new PorgConf(args(0))
    val scheduler = new Scheduler(conf)
    val schedulerThread = new Thread(() => scheduler.run())
    schedulerThread.start()
    val server = new PorgServer(2411, conf, scheduler)
    server.start()
    schedulerThread.join()
  }
}

package edu.porg

import java.lang.Thread

import edu.porg.history.WorkerHistory
import edu.porg.scheduler.PorgScheduler
import edu.porg.webserver.WebServer
import edu.porg.websocketserver.PorgServer

object Porg {
  def main(args: Array[String]): Unit = {
    val porgConf = new PorgConf()

    WorkerHistory.beginWorkerNumberSampleThread(5 * 1000, 10)

    val webServer = new WebServer
    val webServerThread = new Thread(() => webServer.startServer("0.0.0.0", 2426))
    webServerThread.start()

    val scheduler = new PorgScheduler(porgConf)
    val schedulerThread = new Thread(() => scheduler.run())
    schedulerThread.start()

    val webSocketServer = new PorgServer(2411, porgConf, scheduler)
    webSocketServer.start()

    schedulerThread.join()
  }
}


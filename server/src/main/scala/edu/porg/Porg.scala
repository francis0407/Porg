package edu.porg

import java.lang.Thread

import edu.porg.scheduler.PorgScheduler
import edu.porg.webserver.WebServer
import edu.porg.websocketserver.PorgServer

object Porg {
  def main(args: Array[String]): Unit = {
    val porgConf = new PorgConf()

    val webServer = new WebServer
    val webServerThread = new Thread(() => webServer.startServer("0.0.0.0", 2426))
    webServerThread.start()

    val scheduler = new PorgScheduler(porgConf)

    val webSocketServer = new PorgServer(1234, porgConf, scheduler)
    webSocketServer.start()
  }
}


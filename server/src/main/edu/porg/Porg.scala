package edu.porg

import edu.porg.webserver.WebServer

object Porg {
  def main(args: Array[String]): Unit = {
    val webServer = new WebServer
    webServer.startServer("0.0.0.0", 2426)
  }
}


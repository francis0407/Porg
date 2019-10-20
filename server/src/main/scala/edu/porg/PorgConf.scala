package edu.porg

import edu.porg.util.Logging

class PorgConf extends Logging {

  def this(file: String) = {
    this
  }

  def getMaxWaitingJobs: Int = 100 //conf("max_waiting_jobs").toInt

  val conf: Map[String, String] = Map[String, String]()

}

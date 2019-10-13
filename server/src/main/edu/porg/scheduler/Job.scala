package edu.porg.scheduler

import edu.porg.history.MapOnlyJobHistory
import edu.porg.util._

abstract class Job(
    name: String,
    uid: String,
    dir: String,
    program: String,
    inputs: Seq[String]) extends Logging {

  val jid: Int = Job.newId()

  def getName = name

  def init: Unit
}

object Job extends AutoIncreased

case class MapOnlyJob(
    name: String,
    uid: String,
    dir: String,
    program: String,
    inputs: Seq[String])
  extends Job(name, uid, dir, program, inputs) {

  override def init: Unit = {
    MapOnlyJobHistory.newJobHistory(this)
  }
}

case class MapCacheJob(
    name: String,
    uid: String,
    dir: String,
    program: String,
    inputs: Seq[String])
  extends Job(name, uid, dir, program, inputs) {

  override def init: Unit = ???
}

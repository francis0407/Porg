package edu.porg.scheduler

abstract class Task {

}

case class TaskID(jid: Int, tid: Int) {
  override def toString: String = s"job$jid-task$tid"
}

package edu.porg.util

trait AutoIncreased {
  private var auto_id_ = 0
  def newId(): Int = auto_id_.synchronized{
    auto_id_ = auto_id_ + 1
    auto_id_
  }
  def getCurrentID: Int = auto_id_
}

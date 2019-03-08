package org.porg.Util

trait AutoIncreased {
  private var auto_id_ = 0;
  def newId(): Int = {
    auto_id_ = auto_id_ + 1;
    auto_id_
  }
}

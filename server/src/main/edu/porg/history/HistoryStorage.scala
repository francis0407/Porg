package edu.porg.history

import scala.collection.mutable

abstract class HistoryStorage {
  def get(k: String): Option[String]
  def put(k: String, v: String): Unit
  def getAll(prefix: String): Seq[(String, String)]
}

class InMemoryStorage extends HistoryStorage {

  val map: mutable.SortedMap[String, String] = mutable.SortedMap[String, String]()

  override def get(k: String): Option[String] = map.get(k)

  override def put(k: String, v: String): Unit = map.put(k, v)

  override def getAll(prefix: String): Seq[(String, String)] = {
    map.iteratorFrom(prefix).takeWhile(_._1.startsWith(prefix)).toArray
  }
}


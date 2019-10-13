package edu.porg.util

import org.slf4j.LoggerFactory

trait Logging {
  protected val logger = LoggerFactory.getLogger(classOf[Any])
}

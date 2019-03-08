package org.porg.Util

import org.slf4j.LoggerFactory

trait Logging {
  protected val logger = LoggerFactory.getLogger(classOf[Any])
}

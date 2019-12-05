package logger

import org.slf4j.LoggerFactory

trait StandaloneLogger {

  val logger = LoggerFactory.getLogger(this.getClass)

}

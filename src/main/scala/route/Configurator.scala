package route

import com.typesafe.config.ConfigFactory
import logger.StandaloneLogger

import scala.util.{Failure, Success, Try}

object Configurator extends StandaloneLogger {

  def provideAppConfig(): AppConfig = {
    Try {
      val cfg = ConfigFactory.load().getConfig("app")
      val minLen = cfg.getInt("minimumLength")
      val maxNick = cfg.getInt("maxNickLength")
      val maxMail = cfg.getInt("maxEmailLength")
      val maxContent = cfg.getInt("maxContentLength")
      val maxTopic = cfg.getInt("maxTopicLength")
      val maxPagination = cfg.getInt("maxPaginationLimit")
      val host = cfg.getString("host")
      val port = cfg.getInt("port")

      AppConfig(minLen, maxNick, maxMail, maxContent, maxTopic, maxPagination, host, port)
    } match {
      case Success(cfg) =>
        logger.info(
          s"""Successfully loaded config: ${cfg.toString}
            |""".stripMargin)
        cfg
      case Failure(e)   =>
        val default = AppConfig()
        logger.error(
          s"""Config failed to load properly, please check application.conf
             |${e.getMessage}
             |Providing default config:
             |${default.toString}
             |""".stripMargin)
        default
    }
  }
}

case class AppConfig(
                      minimumLength: Int = 1,
                      maxNickLength: Int = 21,
                      maxEmailLength: Int = 254,
                      maxContentLength: Int = 400,
                      maxTopicLength: Int = 80,
                      maxPaginationLimit: Int = 7,
                      host: String = "localhost",
                      port: Int = 8080
                    ) {
  override def toString: String =
    s"""
       |AppConfig:
       |    * host = $host
       |    * port = $port
       |  * minimum field length  = $minimumLength
       |  * max nickname length   = $maxNickLength
       |  * max email length      = $maxEmailLength
       |  * max content length    = $maxContentLength
       |  * max topic length      = $maxTopicLength
       |  * max pagination length = $maxPaginationLimit""".stripMargin
}


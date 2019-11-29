package database

import slick.jdbc.JdbcProfile

trait Profile {
  val jdbcProfile: JdbcProfile
}

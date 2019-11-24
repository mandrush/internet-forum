package domain.logic

import org.apache.commons.lang3.RandomStringUtils

trait SecretGenerator {

  def newSecret: String = RandomStringUtils.random(7, true, true)

}

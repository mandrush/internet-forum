package domain.logic

import database.schema.FieldsValueClasses.Secret
import org.apache.commons.lang3.RandomStringUtils

trait SecretGenerator {

  def newSecret: Secret = Secret(RandomStringUtils.random(7, true, true))

}

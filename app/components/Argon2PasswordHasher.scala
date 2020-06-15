package components

import de.mkammerer.argon2.{Argon2Factory, Argon2Helper}

class Argon2PasswordHasher {

  private val argon2 = Argon2Factory.create()

  private val maxMilliSecs: Long = 500
  private val memory: Int = 65536
  private val parallelism: Int = 1
  private val iterations: Int = Argon2Helper.findIterations(argon2, maxMilliSecs, memory, parallelism)

  def hash(plainPassword: String): String = argon2.hash(iterations, memory, parallelism, plainPassword.toCharArray)

  def matches(userPassword: String, eneteredPassword: String): Boolean = argon2.verify(userPassword, eneteredPassword.toCharArray)

}

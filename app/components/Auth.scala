package components

import java.time.Clock

import javax.inject.Inject
import model.{User, UserClaims}
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim, JwtJson}
import play.api.Configuration
import play.libs.Json

class Auth @Inject()(configuration: Configuration) {

  private def audience: String = configuration.get[String]("auth.audience")
  private def issuer: String = configuration.get[String]("auth.issuer")
  private def expirationTime: Long = configuration.get[Long]("auth.expirationTime")

  def validateJwt(token: String): Option[JwtClaim] = {
    val regex = "^Bearer .*$"
    if (token.matches(regex)) jwtDecode(token) else None
  }

  def generateToken(user: User): String = {
    val someClaims: UserClaims = UserClaims(user.userId, user.email, "")
    val claim =
      JwtClaim(Json.toJson(someClaims).asText).withId(user.userId).by(issuer).to(audience).expiresIn(expirationTime)(Clock.systemUTC)
    Jwt.encode(claim, audience, JwtAlgorithm.HS256)
  }

  def jwtDecode(token: String): Option[JwtClaim] = {
    JwtJson
      .decode(token.replace("Bearer ", ""), audience, Seq(JwtAlgorithm.HS256))
      .map {
        case claims if claims.isValid(issuer, audience)(Clock.systemUTC) => Some(claims)
        case _                                                           => None
      }
      .getOrElse(None)
  }

}

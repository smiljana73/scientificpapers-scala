package components

import model.error.{AuthenticationException, UnauthenticatedException}
import play.api.libs.json.JsObject
import repository._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class SecureContext(token: Option[String],
                         userRepo: UserRepo,
                         scientificPaperRepo: ScientificPaperRepo,
                         auth: Auth,
                         variables: JsObject,
) {

  def login(email: String, password: String): Future[String] =
    userRepo.login(email, password).map {
      case Some(user) => auth.generateToken(user)
      case None       => throw new AuthenticationException
    }

  def authorised[T](fn: String => T): T = token.fold(throw new UnauthenticatedException) { t =>
    auth.validateJwt(t) match {
      case Some(_) => fn(t)
      case None    => throw new UnauthenticatedException
    }
  }

  def getLoggedInUserId(token: String): Option[String] = auth.jwtDecode(token).flatMap(claims => claims.jwtId)

}

package components

import javax.inject.Inject
import play.api.http.HeaderNames
import play.api.mvc.{ActionBuilderImpl, BodyParsers, Request, Result}
import play.api.mvc.Results._

import scala.concurrent.{ExecutionContext, Future}

class AuthActionBuilder @Inject()(auth: Auth, parser: BodyParsers.Default)(implicit ec: ExecutionContext)
    extends ActionBuilderImpl(parser) {

  override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] =
    request.headers.get(HeaderNames.AUTHORIZATION).fold(Future.successful(Forbidden(""))) { token =>
      auth.validateJwt(token) match {
        case Some(_) => block(request)
        case None    => Future.successful(Forbidden(""))
      }
    }

}

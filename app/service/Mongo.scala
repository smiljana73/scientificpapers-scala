package service

import javax.inject.Inject
import model.{Commission, ScientificPaper, User, UserChange}
import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.Cursor
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.gridfs.GridFS
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Mongo @Inject()(val reactiveMongoApi: ReactiveMongoApi) {

  implicit val commissionFormat: OFormat[Commission] = Json.format[Commission]
  implicit val scientificPaperFormat: OFormat[ScientificPaper] = Json.format[ScientificPaper]
  implicit val userFormat: OFormat[User] = Json.format[User]

  val scientificPaperFuture: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection[JSONCollection]("scientificPaper"))
  val userFuture: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection[JSONCollection]("user"))
  val gridFS: Future[GridFS[JSONSerializationPack.type]] = reactiveMongoApi.asyncGridFS

  def findScientificPaper(identificationNumber: String): Future[List[ScientificPaper]] = {
    scientificPaperFuture.flatMap(
      scientificPaper =>
        scientificPaper
          .find(Json.obj("identificationNumber" -> identificationNumber), Option.empty[JsObject])(JsObjectDocumentWriter,
                                                                                                  JsObjectDocumentWriter)
          .cursor[ScientificPaper]()
          .collect[List](-1, Cursor.FailOnError[List[ScientificPaper]]())
    )
  }

  def findUserByEmail(email: String): Future[Option[User]] =
    userFuture.flatMap { user =>
      user
        .find(Json.obj("email" -> email), Option.empty[JsObject])
        .one[User]
    }

  def findUserById(userId: String): Future[Option[User]] =
    userFuture.flatMap { user =>
      user
        .find(Json.obj("userId" -> userId), Option.empty[JsObject])
        .one[User]
    }

  def createUser(userId: String, email: String, firstName: String, lastName: String, password: String): Future[WriteResult] = {
    userFuture.flatMap { user =>
      val newUser = Json.obj("userId" -> userId, "email" -> email, "firstName" -> firstName, "lastName" -> lastName, "password" -> password)
      user.insert(newUser)
    }
  }

  def updatePassword(userId: String, newPassword: String) =
    userFuture.flatMap { user =>
      user
        .update(Json.obj("userId" -> userId), Json.obj("$set" -> Json.obj("password" -> newPassword)))
    }

  def updateUser(userId: String, userChange: UserChange): Future[Option[User]] =
    userFuture.flatMap { user =>
      val update = userChange.firstName.map(fn => Json.obj("firstName" -> fn)).getOrElse(Json.obj()) ++ userChange.lastName
        .map(ln => Json.obj("lastName" -> ln))
        .getOrElse(Json.obj())
      if (update != Json.obj())
        user.findAndUpdate(Json.obj("userId" -> userId), Json.obj("$set" -> update), fetchNewObject = true).map(_.result[User])
      else Future.successful(None)
    }

}

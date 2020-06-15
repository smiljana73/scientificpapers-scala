package controllers

import javax.inject.Inject
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.api.gridfs.ReadFile
import reactivemongo.play.json.JSONSerializationPack
import reactivemongo.play.json._
import service.Mongo
import akka.stream.Materializer
import components.AuthActionBuilder

import scala.concurrent.ExecutionContext.Implicits.global

class FileController @Inject()(val reactiveMongoApi: ReactiveMongoApi,
                               controllerComponents: ControllerComponents,
                               mongo: Mongo,
                               AuthActionBuilder: AuthActionBuilder)(implicit m: Materializer)
    extends AbstractController(controllerComponents)
    with MongoController
    with ReactiveMongoComponents {

  import MongoController.readFileReads
  type JSONReadFile = ReadFile[JSONSerializationPack.type, JsString]

  def getPdf(scientificPaper: String): Action[AnyContent] = AuthActionBuilder.async { request =>
    mongo.gridFS.flatMap { gridFS =>
      val file = gridFS.find[JsObject, JSONReadFile](Json.obj("scientificPaper" -> scientificPaper))
      request.getQueryString("inline") match {
        case Some("true") =>
          serve[JsString, JSONReadFile](gridFS)(file, CONTENT_DISPOSITION_INLINE)
        case _ => serve[JsString, JSONReadFile](gridFS)(file)
      }
    }
  }
}

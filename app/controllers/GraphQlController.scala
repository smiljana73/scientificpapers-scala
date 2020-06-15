package controllers

import components.{Auth, SecureContext}
import javax.inject.Inject
import model.GraphQlSchema
import play.api.Configuration
import play.api.http.HeaderNames
import play.api.libs.json.{JsObject, JsString, JsValue, Json}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents, Request, Result}
import repository.{ScientificPaperRepo, UserRepo}
import sangria.ast.Document
import sangria.execution.{ErrorWithResolver, Executor, QueryAnalysisError}
import sangria.parser.{QueryParser, SyntaxError}
import sangria.schema.Schema
import sangria.marshalling.playJson._
import sangria.renderer.SchemaRenderer

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class GraphQlController @Inject()(userRepo: UserRepo,
                                  scientificPaperRepo: ScientificPaperRepo,
                                  auth: Auth,
                                  configuration: Configuration,
                                  val controllerComponents: ControllerComponents)
    extends BaseController {

  lazy val schema: Schema[SecureContext, Unit] = new GraphQlSchema(configuration).schema

  def graphiql: Action[AnyContent] = Action {
    Ok(views.html.graphiql())
  }

  def graphql: Action[JsValue] = Action.async(parse.json) { implicit request =>
    def parseVariables(variables: String) =
      if (variables.trim == "" || variables.trim == "null") Json.obj()
      else Json.parse(variables).as[JsObject]

    val query = (request.body \ "query").as[String]
    val operation = (request.body \ "operationName").asOpt[String]
    val variables = (request.body \ "variables").toOption
      .map {
        case JsString(vars) => parseVariables(vars)
        case obj: JsObject  => obj
        case _              => Json.obj()
      }
      .getOrElse(Json.obj())

    QueryParser.parse(query) match {
      case Success(queryAst) =>
        executeGraphQLQuery(queryAst, operation, variables)
      case Failure(error: SyntaxError) =>
        Future.successful(BadRequest(Json.obj("error" -> error.getMessage)))
      case Failure(error) => throw error
    }
  }

  private def executeGraphQLQuery(query: Document, op: Option[String], variables: JsObject)(implicit request: Request[_]): Future[Result] =
    Executor
      .execute(
        schema,
        query,
        SecureContext(request.headers.get(HeaderNames.AUTHORIZATION), userRepo, scientificPaperRepo, auth, variables),
        operationName = op,
        variables = variables
      )
      .map(Ok(_))
      .recover {
        case error: QueryAnalysisError => BadRequest(error.resolveError)
        case error: ErrorWithResolver  => InternalServerError(error.resolveError)
      }

}

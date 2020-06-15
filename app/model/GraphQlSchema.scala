package model

import components.SecureContext
import play.api.Configuration
import play.api.libs.json.{JsObject, Json, OFormat}
import sangria.ast.{ListValue, ObjectValue, StringValue, Value, VariableValue}
import sangria.schema._
import sangria.macros.derive._
import sangria.schema.InputObjectType.DefaultInput
import sangria.marshalling.playJson.playJsonReaderFromInput

case class FilterConfig(key: String, name: String)

class GraphQlSchema(configuration: Configuration) {

  implicit val pageParametersFormat: OFormat[PageParameter] = Json.format[PageParameter]
  implicit val userChangeFormat: OFormat[UserChange] = Json.format[UserChange]
  implicit val userRegisterFormat: OFormat[UserRegister] = Json.format[UserRegister]

  val FilterParametersType: InputObjectType[DefaultInput] = InputObjectType(
    "Filters",
    fields = configuration
      .get[Configuration]("graphql.filters")
      .subKeys
      .map(key => InputField(key, OptionInputType(ListInputType(StringType))))
      .toList
  )
  val PageParameterType: InputObjectType[PageParameter] = deriveInputObjectType[PageParameter]()
  val UserChangeType: InputObjectType[UserChange] = deriveInputObjectType[UserChange]()
  val UserRegisterType: InputObjectType[UserRegister] = deriveInputObjectType[UserRegister]()

  val FilterParametersArg: Argument[Option[DefaultInput]] = Argument("filter", OptionInputType(FilterParametersType))
  val FulltextArg: Argument[String] = Argument("fulltext", StringType)
  val IdentificationNumberArg: Argument[String] = Argument("identificationNumber", StringType)
  val PageParameterArg: Argument[Option[PageParameter]] = Argument("page", OptionInputType(PageParameterType))
  val UserChangeArg: Argument[UserChange] = Argument("user", UserChangeType)
  val UserRegisterArg: Argument[UserRegister] = Argument("user", UserRegisterType)

  implicit val CommissionType: ObjectType[Unit, Commission] = deriveObjectType[Unit, Commission]()
  implicit val FilterValueType: ObjectType[Unit, FilterValue] = deriveObjectType[Unit, FilterValue]()
  implicit val FilterType: ObjectType[Unit, Filter] = deriveObjectType[Unit, Filter]()
  implicit val ScientificPaperResultType: ObjectType[Unit, ScientificPaperResult] = deriveObjectType[Unit, ScientificPaperResult]()

  val ScientificPaperType: ObjectType[Unit, ScientificPaper] = deriveObjectType[Unit, ScientificPaper]()
  val GraphQlResultType: ObjectType[Unit, GraphQlResult] = deriveObjectType[Unit, GraphQlResult]()
  val UserInfoType: ObjectType[Unit, UserInfo] = deriveObjectType[Unit, UserInfo]()

  val QueryType: ObjectType[SecureContext, Unit] = ObjectType(
    "Query",
    fields[SecureContext, Unit](
      Field(
        "scientificPaper",
        ListType(ScientificPaperType),
        description = Some("Returns scientific paper with specified identification number"),
        arguments = IdentificationNumberArg :: Nil,
        resolve = ctx =>
          ctx.ctx.authorised { _ =>
            ctx.ctx.scientificPaperRepo.findScientificPaper(ctx arg IdentificationNumberArg)
        }
      ),
      Field(
        "queryScientificPaper",
        GraphQlResultType,
        description = Some("Queries scientific papers based on fulltext and selected filters"),
        arguments = FulltextArg :: PageParameterArg :: FilterParametersArg :: Nil,
        resolve = ctx => {
          ctx.ctx.authorised {
            _ =>
              def getFilterValue(value: Value, variables: JsObject): Seq[String] = value match {
                case stringValue: StringValue => Seq(stringValue.asInstanceOf[StringValue].value)
                case listValue: ListValue =>
                  listValue.values
                    .filter(_.getClass.getName == "sangria.ast.StringValue")
                    .map(_.asInstanceOf[StringValue])
                    .map(_.value)
                case variableValue: VariableValue => (variables \ variableValue.name).as[Seq[String]]
                case _                            => Seq("")
              }

              val filters = ctx.astFields.headOption.flatMap(
                _.arguments
                  .find(arg => arg.name == "filter")
                  .map(_.value.asInstanceOf[ObjectValue].fieldsByName)
                  .map(_.map(x => FilterParameter(x._1, getFilterValue(x._2, ctx.ctx.variables))).toSeq)
              )
              ctx.ctx.scientificPaperRepo.queryScientificPaper(ctx.arg(FulltextArg), ctx.arg(PageParameterArg), filters)
          }
        }
      ),
      Field(
        "getUser",
        OptionType(UserInfoType),
        resolve = ctx => {
          ctx.ctx.authorised { token =>
            ctx.ctx.userRepo.findUserById(ctx.ctx.getLoggedInUserId(token))
          }
        }
      )
    )
  )

  val UserNameArg: Argument[String] = Argument("userName", StringType)
  val PasswordArg: Argument[String] = Argument("password", StringType)

  val MutationType: ObjectType[SecureContext, Unit] = ObjectType(
    "Mutation",
    fields[SecureContext, Unit](
      Field(
        "login",
        OptionType(StringType),
        arguments = UserNameArg :: PasswordArg :: Nil,
        resolve = ctx =>
          UpdateCtx(ctx.ctx.login(ctx.arg(UserNameArg), ctx.arg(PasswordArg))) { token =>
            ctx.ctx.copy(token = Some(token))
        }
      ),
      Field(
        "register",
        OptionType(StringType),
        arguments = UserRegisterArg :: Nil,
        resolve = ctx => ctx.ctx.userRepo.register(ctx.arg(UserRegisterArg))
      ),
      Field(
        "updateUser",
        OptionType(UserInfoType),
        arguments = UserChangeArg :: Nil,
        resolve = ctx => {
          ctx.ctx.authorised { token =>
            ctx.ctx.userRepo.updateUser(ctx.ctx.getLoggedInUserId(token), ctx.arg(UserChangeArg))
          }
        }
      ),
      Field(
        "forgotPassword",
        StringType,
        arguments = UserNameArg :: Nil,
        resolve = ctx => ctx.ctx.userRepo.forgotPassword(ctx.arg(UserNameArg))
      )
    )
  )

  val schema: Schema[SecureContext, Unit] = Schema(QueryType, Some(MutationType))

}

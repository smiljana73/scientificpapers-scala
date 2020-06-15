package model

case class FilterParameter(
    name: String,
    selectedValue: Seq[String]
)

case class PageParameter(
    from: Option[Int],
    size: Option[Int]
)

case class FilterValue(
    value: String,
    count: Long
)

case class Filter(
    key: String,
    name: String,
    values: Seq[FilterValue]
)

case class ScientificPaperResult(
    identificationNumber: String,
    title: String,
    documentType: String,
    mentor: String,
    author: String,
    year: String,
    publisher: String,
    scientificField: String,
    description: String
)

case class GraphQlResult(
    scientificPapers: Seq[ScientificPaperResult],
    filters: Seq[Filter],
    totalCount: Long
)

case class UserInfo(
    email: String,
    firstName: String,
    lastName: String
)

case class UserChange(
    firstName: Option[String],
    lastName: Option[String],
    oldPassword: Option[String],
    newPassword: Option[String],
    confirmPassword: Option[String]
)

case class UserRegister(
    email: String,
    firstName: String,
    lastName: String,
    password: String,
    confirmPassword: String
)

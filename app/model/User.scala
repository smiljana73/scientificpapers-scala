package model

case class User(userId: String, email: String, password: String, firstName: String, lastName: String)

case class UserClaims(userId: String, username: String, userToken: String)

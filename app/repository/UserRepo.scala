package repository

import components.Argon2PasswordHasher
import javax.inject.Inject
import model.error._
import model.{User, UserChange, UserInfo, UserRegister}
import service.{MailerService, Mongo}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class UserRepo @Inject()(mongo: Mongo, mailerService: MailerService, argon2PasswordHasher: Argon2PasswordHasher) {

  def findUserById(userId: Option[String]): Future[Option[UserInfo]] =
    userId
      .map(mongo.findUserById)
      .getOrElse(Future.successful(None))
      .map(_.map { user =>
        UserInfo(user.email, user.firstName, user.lastName)
      })

  def login(email: String, password: String): Future[Option[User]] =
    mongo
      .findUserByEmail(email)
      .map(
        _.flatMap {
          case user if argon2PasswordHasher.matches(user.password, password) => Some(user)
          case _                                                             => None
        }
      )

  def register(userRegister: UserRegister): Future[String] =
    if (userRegister.productIterator.exists(f => f.asInstanceOf[String].isEmpty)) throw new EmptyFields
    else {
      mongo.findUserByEmail(userRegister.email).flatMap {
        case Some(_) => throw new ExistingUser
        case None =>
          if (userRegister.password.length < 8) throw new PasswordLengthException
          if (userRegister.password != userRegister.confirmPassword) throw new PasswordMatchException
          val writeRes =
            mongo.createUser(randomString(20),
                             userRegister.email,
                             userRegister.firstName,
                             userRegister.lastName,
                             argon2PasswordHasher.hash(userRegister.password))
          writeRes.onComplete {
            case Failure(_) => throw new ErrorCreatingUser
            case Success(_) => "User successfully created"
          }
          writeRes.map(_ => "User successfully created")
      }
    }

  def updateUser(userId: Option[String], userChange: UserChange): Future[Option[UserInfo]] =
    mongo.findUserById(userId.getOrElse("")).flatMap {
      case Some(user) =>
        if (userChange.productIterator.exists(f => f.isInstanceOf[Some[_]] && f.asInstanceOf[Some[_]].get.asInstanceOf[String].isEmpty))
          throw new EmptyFields
        for {
          oldPassword <- userChange.oldPassword
          newPassword <- userChange.newPassword
          confirmPassword <- userChange.confirmPassword
        } yield {
          if (newPassword.length < 8) throw new PasswordLengthException
          if (!argon2PasswordHasher.matches(user.password, oldPassword)) throw new IncorrectOldPasswordException
          if (argon2PasswordHasher.matches(user.password, newPassword)) throw new SamePasswordException
          if (newPassword != confirmPassword) throw new PasswordMatchException
          mongo.updatePassword(user.userId, argon2PasswordHasher.hash(newPassword))
        }
        mongo
          .updateUser(user.userId, userChange)
          .map(_.map { newUser =>
            UserInfo(newUser.email, newUser.firstName, newUser.lastName)
          })
      case None => Future.successful(None)
    }

  def forgotPassword(email: String): Future[String] = {
    mongo
      .findUserByEmail(email)
      .map(
        _.map { user =>
          val newPassword = randomString(10)
          mailerService.sendNewPasswordEmail(email, newPassword)
          mongo.updatePassword(user.userId, argon2PasswordHasher.hash(newPassword))
          "New password has been sent to the email address you provided"
        }.getOrElse(throw new NonExistingUser)
      )

  }

  private def randomString(len: Int): String = {
    val rand = new scala.util.Random(System.nanoTime)
    val sb = new StringBuilder(len)
    val ab = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
    for (i <- 0 until len) {
      sb.append(ab(rand.nextInt(ab.length)))
    }
    sb.toString
  }

}

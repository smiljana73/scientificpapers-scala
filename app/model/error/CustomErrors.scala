package model.error

import sangria.execution.{ExceptionHandler, ExecutionError}

case class AuthenticationException(message: String = "UserName or password is incorrect", eh: ExceptionHandler = ExceptionHandler.empty)
    extends ExecutionError(message = message, exceptionHandler = eh) {}

case class UnauthenticatedException(message: String = "Invalid token", eh: ExceptionHandler = ExceptionHandler.empty)
    extends ExecutionError(message = message, exceptionHandler = eh) {}

case class PasswordLengthException(message: String = "Password must be at least 8 characters long",
                                   eh: ExceptionHandler = ExceptionHandler.empty)
    extends ExecutionError(message = message, exceptionHandler = eh) {}

case class IncorrectOldPasswordException(message: String = "Old password is incorrect", eh: ExceptionHandler = ExceptionHandler.empty)
    extends ExecutionError(message = message, exceptionHandler = eh) {}

case class SamePasswordException(message: String = "New password can't be old password", eh: ExceptionHandler = ExceptionHandler.empty)
    extends ExecutionError(message = message, exceptionHandler = eh) {}

case class PasswordMatchException(message: String = "Password and confirmation password do not match",
                                  eh: ExceptionHandler = ExceptionHandler.empty)
    extends ExecutionError(message = message, exceptionHandler = eh) {}

case class NonExistingUser(message: String = "Account address doesn't exist", eh: ExceptionHandler = ExceptionHandler.empty)
    extends ExecutionError(message = message, exceptionHandler = eh) {}

case class ExistingUser(message: String = "User already exists", eh: ExceptionHandler = ExceptionHandler.empty)
    extends ExecutionError(message = message, exceptionHandler = eh) {}

case class EmptyFields(message: String = "Please fill in all fields", eh: ExceptionHandler = ExceptionHandler.empty)
    extends ExecutionError(message = message, exceptionHandler = eh) {}

case class ErrorCreatingUser(message: String = "Error creating user", eh: ExceptionHandler = ExceptionHandler.empty)
    extends ExecutionError(message = message, exceptionHandler = eh) {}

package service

import play.api.libs.mailer._
import javax.inject.Inject

class MailerService @Inject()(mailerClient: MailerClient) {

  def sendNewPasswordEmail(to: String, newPassword: String): Unit = {
    val email = Email(
      subject = "New password",
      from = "Admin <admin@scientificPapers.com>",
      to = Seq(to),
      bodyHtml = Some(s"<p>Greetings,</p><p>Your new password has been set to $newPassword</p><p>Sincerely, Scientific Papers</p>")
    )
    mailerClient.send(email)
  }

}

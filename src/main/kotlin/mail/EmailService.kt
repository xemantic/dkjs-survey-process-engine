package de.dkjs.survey.mail

import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.mail.javamail.MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED
import org.springframework.stereotype.Component
import org.thymeleaf.context.Context
import org.thymeleaf.spring5.SpringTemplateEngine
import javax.inject.Inject
import javax.inject.Singleton
import javax.mail.MessagingException

// TODO Delete this file (EML_TEST)
@Singleton
@Component
class EmailSenderService @Inject constructor(
    private val mailSender: JavaMailSender,
    private val templateEngine: SpringTemplateEngine
) {

    @Throws(MessagingException::class)
    fun send() {
        // 1. Template processing
        val ctx = Context().apply {
            setVariable("projectName", "Fix the world")
            setVariable("projectNumber", 12345)
            setVariable("startDate", "01.01.2001")
            setVariable("endDate", "31.12.2222")
            setVariable("formLink", "https://archive.org/")
            setVariable("pdfLink", "https://dagrs.berkeley.edu/sites/default/files/2020-01/sample.pdf")
        }

        val templatePath = "mail/infomail_pre_post"

        val bodyHTML = templateEngine.process("$templatePath/body.html", ctx)
        val subject = templateEngine.process("$templatePath/subject.txt", ctx)

        // 2. Construct e-mail
        val message = mailSender.createMimeMessage()
        val mimeMessage = MimeMessageHelper(
            message, MULTIPART_MODE_MIXED_RELATED, "UTF-8"
        ).apply {
            setFrom("server@hamoid.com")
            setTo("abe@hamoid.com")
            setSubject(subject)
            setText(bodyHTML, true)

            // Inline image
            // setText(text,"my text &lt;img src='cid:myLogo'&gt;")
            // addInline("myLogo", ClassPathResource("img/mylogo.gif"))

            // Attachment
            // addAttachment("myDocument.pdf", ClassPathResource("doc/myDocument.pdf"))
        }

        // 3. Send
        mailSender.send(message)
    }
}
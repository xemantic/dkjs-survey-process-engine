/*
 * dkjs-survey-process-engine - https://www.dkjs.de/
 * Copyright (C) 2022 Kazimierz Pogoda / https://xemantic.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.dkjs.survey.mail

import de.dkjs.survey.test.DkjsSurveyProcessEngineTest
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper

@DkjsSurveyProcessEngineTest
class AreWeASpammerTest(
  @Autowired private val mailSender: JavaMailSender
) {

  @Test
  @Disabled // intentionally disabled as it is a one time action run to verify
  fun `send email to mail tester`() {
    val from = "evaluation.aufleben@dkjs.de"
    val to = "test-m9kx58dw5@srv1.mail-tester.com" // replace with the mail supplied by the tester
    val message = mailSender.createMimeMessage()
    MimeMessageHelper(
      message,
      MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
      "UTF-8"
    ).run {
      setFrom(from)
      setTo(to)
      setSubject("test")
      setText("""
        <html>
          <body>
            <p>
              foo
            </p>
          </body>
        </html>
        """".trimIndent()
      )
    }
    mailSender.send(message)
  }

}

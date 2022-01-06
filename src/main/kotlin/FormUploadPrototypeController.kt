/*
 * Copyright (c) 2022 Abe Pazos - https://hamoid.com
 */

package de.dkjs.survey

import de.dkjs.survey.de.dkjs.survey.model.Person
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.util.StringUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption


@Controller
class FormUploadPrototypeController {
    private val uploadDir = "./uploads/"

    @GetMapping()
    fun index(model: Model): String {
        model.addAttribute("time", System.currentTimeMillis().toString())
        model.addAttribute(
            "people", listOf(
                Person("A", 1),
                Person("B", 2),
                Person("C", 3)
            )
        )
        return "index"
    }

    @PostMapping("/upload")
    fun uploadFile(
        @RequestParam("file") file: MultipartFile,
        attributes: RedirectAttributes
    ): String {
        if (file.isEmpty) {
            attributes.addFlashAttribute(
                "message",
                "Please select a file to upload."
            )
            return "redirect:/"
        }

        val fileName = StringUtils.cleanPath(file.originalFilename!!)

        try {
            val path = Paths.get(uploadDir + fileName)
            println("target: $path")
            Files.copy(
                file.inputStream, path, StandardCopyOption.REPLACE_EXISTING
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }

        attributes.addFlashAttribute(
            "message",
            "You successfully uploaded $fileName!"
        )
        return "redirect:/"
    }
}
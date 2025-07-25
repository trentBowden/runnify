// Hello world controller
package dev.trentbowden.runnify.controller

import dev.trentbowden.runnify.service.PageViewService
import dev.trentbowden.runnify.utilities.GpxParser
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile

@Controller
class UploadPageController(private val pageViewService: PageViewService) {
    @GetMapping("/upload")

    /** Upload page Allows the user to upload a GPX file for processing */
    fun showUploadPage(): String {
        return "upload"
    }

    /** Upload ?complete? page Shows the results of the upload */
    @PostMapping("/upload")
    fun uploadFile(@RequestParam("file") file: MultipartFile, model: Model): String {
        val gpx = GpxParser().parse(file)
        model.addAttribute("gpx", gpx)
        return "result"
    }
}

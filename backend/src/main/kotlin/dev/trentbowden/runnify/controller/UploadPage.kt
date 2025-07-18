// Hello world controller
package dev.trentbowden.runnify.controller

import dev.trentbowden.runnify.service.PageViewService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile

@Controller
class UploadPageController(private val pageViewService: PageViewService) {
    @GetMapping("/upload")
    fun showUploadPage(): String {
        return "upload"
    }

    @PostMapping("/upload")
    fun uploadFile(@RequestParam("file") file: MultipartFile, model: Model): String {
        model.addAttribute("file", file)
        return "result"
    }
}

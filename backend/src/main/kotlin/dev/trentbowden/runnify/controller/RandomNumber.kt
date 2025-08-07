// Hello world controller
package dev.trentbowden.runnify.controller

import dev.trentbowden.runnify.entity.PageView
import dev.trentbowden.runnify.service.PageViewService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin(origins = ["http://127.0.0.1:5173"])
class RandomNumberController() {
    @GetMapping("/randomNumber")
    fun randomNumber(): Number {
        return (1..100).random()
    }
}

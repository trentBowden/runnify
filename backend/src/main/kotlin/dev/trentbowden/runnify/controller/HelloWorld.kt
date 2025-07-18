// Hello world controller
package dev.trentbowden.runnify.controller

import dev.trentbowden.runnify.entity.PageView
import dev.trentbowden.runnify.service.PageViewService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloWorldController(private val pageViewService: PageViewService) {
    @GetMapping("/hello")
    fun hello(httpRequest: HttpServletRequest): String {
        pageViewService.savePageView(
                PageView(
                        pageUrl = httpRequest.requestURI ?: "unknown",
                        ipAddress = httpRequest.remoteAddr
                )
        )

        val pageViews = pageViewService.getPageViewsByUrl(httpRequest.requestURI ?: "unknown")
        return "Hello, World!! This page has been viewed about ${pageViews.size} times."
    }
}

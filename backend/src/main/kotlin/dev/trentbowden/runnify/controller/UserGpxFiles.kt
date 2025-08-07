// Hello world controller
package dev.trentbowden.runnify.controller

import dev.trentbowden.runnify.entity.PageView
import dev.trentbowden.runnify.service.Gpx.GpxParsed
import dev.trentbowden.runnify.service.PageViewService
import dev.trentbowden.runnify.entity.Playlist
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin(origins = ["http://localhost:5173"])
class UserGpxFilesController() {
    @GetMapping("/gpxFiles")
    fun getGpxFiles(): List<GpxParsed> {
        return listOf(
            GpxParsed(
                name = "Horses",
                description = "A run of horses",
                distance = 100.0,
                elevationGain = 100.0,
                elevationLoss = 0.0,
                hills = emptyList(),
                trackPoints = emptyList(),
                trackPointsCartesian = emptyList(),
            ),
        );
    }
}

package dev.trentbowden.runnify.service.Gpx

import dev.trentbowden.runnify.utilities.Hill
import dev.trentbowden.runnify.utilities.TrackPoint

data class GpxParsed(
        val name: String,
        val description: String,
        val distance: Double,
        val elevationGain: Double,
        val elevationLoss: Double,
        val hills: List<Hill>,
        val trackPoints: List<TrackPoint>
)

package dev.trentbowden.runnify.service.Gpx

import dev.trentbowden.runnify.utilities.Hill
import dev.trentbowden.runnify.utilities.TrackPoint
import dev.trentbowden.runnify.utilities.TrackPointCartesian

data class GpxParsed(
        val name: String,
        val description: String,
        val distance: Double,
        val elevationGain: Double,
        val elevationLoss: Double,
        val hills: List<Hill>,
        val trackPoints: List<TrackPoint>,
        val trackPointsCartesian: List<TrackPointCartesian>
)

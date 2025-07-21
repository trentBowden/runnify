package dev.trentbowden.runnify.service.Gpx

data class GpxParsed(
        val name: String,
        val description: String,
        val distance: Double,
        val elevationGain: Double,
        val elevationLoss: Double,
)

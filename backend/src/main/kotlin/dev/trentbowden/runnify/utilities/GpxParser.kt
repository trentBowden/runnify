package dev.trentbowden.runnify.utilities

import dev.trentbowden.runnify.service.Gpx.GpxParsed
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.math.*
import org.springframework.web.multipart.MultipartFile

data class Hill(val startIndex: Int, val endIndex: Int, val gain: Double)

data class TrackPoint(val lat: Double, val lon: Double, val elevation: Double)

data class TrackPointCartesian(val x: Double, val y: Double, val z: Double)

class GpxParser {

    // To calculate the distance between two points on the earth's surface, we use the haversine
    // formula.
    // https://en.wikipedia.org/wiki/Haversine_formula
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371e3 // Earth radius in meters
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val deltaPhi = Math.toRadians(lat2 - lat1)
        val deltaLambda = Math.toRadians(lon2 - lon1)

        val a = sin(deltaPhi / 2).pow(2.0) + cos(phi1) * cos(phi2) * sin(deltaLambda / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c // Distance in meters
    }

    private fun trackPointToCartesian(
            referencePoint: TrackPoint,
            trackPoint: TrackPoint
    ): TrackPointCartesian {
        val R = 6371000.0 // Earth radius in meters

        val lat0Rad = Math.toRadians(referencePoint.lat)
        val dLat = Math.toRadians(trackPoint.lat - referencePoint.lat)
        val dLon = Math.toRadians(trackPoint.lon - referencePoint.lon)

        val x = dLon * cos(lat0Rad) * R
        val y = dLat * R
        val z = trackPoint.elevation - referencePoint.elevation

        // Swapping y and z coordinates
        // Otherwise the flythrough looks like a rollercoaster.
        return TrackPointCartesian(x, z, y)
    }

    fun findHills(elevations: List<Double>, minGain: Double = 10.0): List<Hill> {
        val hills = mutableListOf<Hill>()
        var climbing = false
        var startIdx = 0
        var startElevation = 0.0
        var maxElevation = 0.0

        for (i in 1 until elevations.size) {
            val diff = elevations[i] - elevations[i - 1]
            if (diff > 0) {
                if (!climbing) {
                    climbing = true
                    startIdx = i - 1
                    startElevation = elevations[startIdx]
                    maxElevation = elevations[i]
                } else {
                    maxElevation = maxOf(maxElevation, elevations[i])
                }
            } else if (climbing && diff <= 0) {
                // End of climb
                val gain = maxElevation - startElevation
                if (gain >= minGain) {
                    hills.add(Hill(startIdx, i - 1, gain))
                }
                climbing = false
            }
        }
        // Handle if the last segment is a climb
        if (climbing) {
            val gain = maxElevation - startElevation
            if (gain >= minGain) {
                hills.add(Hill(startIdx, elevations.size - 1, gain))
            }
        }
        return hills
    }

    fun parse(file: MultipartFile): GpxParsed {
        val fileName = file.originalFilename
        val extension = fileName?.substring(fileName.lastIndexOf(".") + 1)

        if (extension != "gpx") {
            throw IllegalArgumentException("File must be a GPX file")
        }

        // The GPX file exists. Let's read it as an XML.
        val inputStream = file.inputStream
        val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc = docBuilder.parse(inputStream)

        // 1. Get the name and description, if any
        val name = doc.getElementsByTagName("name")?.item(0)?.textContent ?: fileName
        val description =
                doc.getElementsByTagName("desc")?.item(0)?.textContent ?: "An uploaded file"

        // 2. Calculate the elevation gain and loss.
        var elevationGain = 0.0
        var elevationLoss = 0.0

        // I was getting a lot of noise in the elevation data, so I'm going to ignore
        // any changes less than X meters.
        val elevationThreshold = 0.08

        val elevationTags = doc.getElementsByTagName("ele")
        if (elevationTags.length > 0) {
            for (i in 1 until elevationTags.length) {
                val thisElevation = elevationTags.item(i).textContent?.toDouble() ?: 0.0

                val lastElevation =
                        if (i == 0) 0.0
                        else elevationTags.item(i - 1).textContent?.toDouble() ?: 0.0

                // Assuming that no elevations are negative..
                if (thisElevation > lastElevation &&
                                thisElevation - lastElevation > elevationThreshold
                ) {
                    elevationGain += thisElevation - lastElevation
                } else if (thisElevation < lastElevation &&
                                lastElevation - thisElevation > elevationThreshold
                ) {
                    elevationLoss += lastElevation - thisElevation
                }
            }
        }

        // 3. Calculate the distance.
        // Distance is given in lat/lon with trackpoints (trkpt tags)
        // ie <trkpt lat="-44.945495" lon="168.819644">
        // This assumes that all trackpoints have lat/lon.
        val trackPoints = doc.getElementsByTagName("trkpt")
        var totalDistance = 0.0
        if (trackPoints.length > 0) {
            for (i in 1 until trackPoints.length) {
                // Skip the first trackpoint, we only care about the delta.
                if (i == 0) {
                    continue
                }

                val thisTrackPoint = trackPoints.item(i)
                val thisLat =
                        thisTrackPoint.attributes.getNamedItem("lat")?.textContent?.toDouble()
                                ?: 0.0
                val thisLon =
                        thisTrackPoint.attributes.getNamedItem("lon")?.textContent?.toDouble()
                                ?: 0.0

                // We're assuming each trackpoint has a lat and lon.
                if (thisLat == null || thisLon == null) {
                    continue
                }

                val lastTrackPoint = trackPoints.item(i - 1)

                val lastLat =
                        lastTrackPoint.attributes.getNamedItem("lat")?.textContent?.toDouble()
                                ?: 0.0
                val lastLon =
                        lastTrackPoint.attributes.getNamedItem("lon")?.textContent?.toDouble()
                                ?: 0.0

                val distance = calculateDistance(thisLat, thisLon, lastLat, lastLon)

                // Add the distance to the total distance.
                totalDistance += distance
            }
        }

        val elevations =
                (0 until elevationTags.length).map { i ->
                    elevationTags.item(i).textContent?.toDouble() ?: 0.0
                }
        val hills = findHills(elevations)

        val myTrackPoints =
                (0 until trackPoints.length).map { i ->
                    val thisTrackPoint = trackPoints.item(i)
                    val thisLat =
                            thisTrackPoint.attributes.getNamedItem("lat")?.textContent?.toDouble()
                                    ?: 0.0
                    val thisLon =
                            thisTrackPoint.attributes.getNamedItem("lon")?.textContent?.toDouble()
                                    ?: 0.0
                    val thisElevation = elevationTags.item(i).textContent?.toDouble() ?: 0.0
                    TrackPoint(thisLat, thisLon, thisElevation)
                }

        val referencePoint = myTrackPoints.first()
        val myTrackPointsCartesian =
                myTrackPoints.map { trackPoint ->
                    trackPointToCartesian(referencePoint, trackPoint)
                }

        val gpx =
                GpxParsed(
                        name,
                        description,
                        totalDistance.roundToInt().toDouble(),
                        elevationGain.roundToInt().toDouble(),
                        elevationLoss.roundToInt().toDouble(),
                        hills,
                        myTrackPoints,
                        myTrackPointsCartesian
                )

        return gpx
    }
}

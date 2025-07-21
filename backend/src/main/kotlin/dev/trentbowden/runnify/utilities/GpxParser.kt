package dev.trentbowden.runnify.utilities

import dev.trentbowden.runnify.service.Gpx.GpxParsed
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.math.*
import org.springframework.web.multipart.MultipartFile

class GpxParser {
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

        // The structure of a GPX file is:
        // https://en.wikipedia.org/wiki/GPS_Exchange_Format
        // <gpx>
        //   <metadata>
        //     <name>Data name</name>
        //     <desc>Valid GPX example without special characters</desc>
        //     <author>
        //       <name>Author name</name>
        //     </author>
        //   </metadata>
        //   <wpt>
        //     <ele>35.0</ele>
        //     <time>2011-12-31T23:59:59Z</time>
        //     <name>Reichstag (Berlin)</name>
        //     <sym>City</sym>
        //   </wpt>
        // </gpx>

        // 1. Get the name and description, if any
        val name = doc.getElementsByTagName("name")?.item(0)?.textContent ?: fileName
        val description =
                doc.getElementsByTagName("desc")?.item(0)?.textContent ?: "An uploaded file"

        // 2. Calculate the elevation gain and loss.
        var elevationGain = 0.0
        var elevationLoss = 0.0

        val elevationTags = doc.getElementsByTagName("ele")
        if (elevationTags.length > 0) {
            for (i in 1 until elevationTags.length) {
                val thisElevation = elevationTags.item(i).textContent?.toDouble() ?: 0.0

                val lastElevation =
                        if (i == 0) 0.0
                        else elevationTags.item(i - 1).textContent?.toDouble() ?: 0.0

                // Assuming that no elevations are negative..
                if (thisElevation > lastElevation) {
                    elevationGain += thisElevation - lastElevation
                } else {
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

        val gpx =
                GpxParsed(
                        name,
                        description,
                        totalDistance.roundToInt().toDouble(),
                        elevationGain.roundToInt().toDouble(),
                        elevationLoss.roundToInt().toDouble()
                )

        return gpx
    }
}

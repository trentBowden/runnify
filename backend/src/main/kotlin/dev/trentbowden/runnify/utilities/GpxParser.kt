package dev.trentbowden.runnify.utilities

import dev.trentbowden.runnify.service.Gpx.GpxParsed
import javax.xml.parsers.DocumentBuilderFactory
import org.springframework.web.multipart.MultipartFile

class GpxParser {
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

        // 1. Get the name, if any
        val name = doc.getElementsByTagName("name")?.item(0)?.textContent ?: fileName
        val description =
                doc.getElementsByTagName("desc")?.item(0)?.textContent ?: "An uploaded file"

        val gpx = GpxParsed(name, description, 0.0, 0.0)

        return gpx
    }
}

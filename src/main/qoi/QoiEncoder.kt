package main.qoi

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

class QoiEncoder(private val image: QoiImage) {
    /**
     * Writes the QOI-encoded image to a given file.
     *
     * @param file: The file to write the QOI image in
     */
    fun writeFile(file: File) {
        val buffer = BufferedOutputStream(FileOutputStream(file))
        buffer.write(image.getQoiHeader())

        var runLength = 0
        var previous = QoiPixel(0.toUByte(), 0.toUByte(), 0.toUByte(), 0xFF.toUByte())

        for (idx in 0..(image.size - image.channels) step image.channels) {
            val pixel = image.getPixel(idx)

            if (pixel == previous) {
                runLength ++

                if (runLength == 62) {
                    buffer.write(QOICodec.QOI_OP_RUN or (runLength - 1))
                    runLength = 0
                }
            } else {
                if (runLength > 0) {
                    buffer.write(QOICodec.QOI_OP_RUN or (runLength - 1))
                    runLength = 0
                }

                buffer.write(image.QOI_PIXEL_PREFIX)
                buffer.write(pixel.bytes.slice(0 until image.channels).toByteArray()) // TODO: Fix this bs
            }

            previous = pixel
        }

        buffer.write(QOICodec.QOI_ENDF)
        buffer.close()
    }
}

fun main(args: Array<String>) {
    val inputFile = "testcard"
    val image = QoiImage.fromFile("assets/${inputFile}.png")
    val encoder = QoiEncoder(image)

    val out = File("converted/${inputFile}.qoi")
    out.mkdirs()
    out.delete()
    encoder.writeFile(out)
}
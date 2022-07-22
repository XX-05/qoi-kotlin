package main.qoi

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

class QoiEncoder(private val image: QoiImage) {
    private fun tinyDifference(diff: Byte): Boolean {
        return (-3 < diff) && (diff < 2)
    }

    private fun smallerDifference(diff: Int): Boolean {
        return (-9 < diff) && (diff < 8)
    }

    private fun smallDifference(diff: Byte): Boolean {
        return (-33 < diff) && (diff < 32)
    }
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
        val index = Array(64) { QoiPixel(0.toUByte(), 0.toUByte(), 0.toUByte()) }

        for (idx in 0..(image.size - image.channels) step image.channels) {
            val pixel = image.getPixel(idx)

            if (pixel == previous) { // check for a run
                if ((runLength++) == 62) {
                    buffer.write(QoiCodec.QOI_OP_RUN or (runLength - 1))
                    runLength = 0
                }
            } else {
                val pixelHash = pixel.getHash()

                if (runLength > 0) { // write pixel run
                    buffer.write(QoiCodec.QOI_OP_RUN or (runLength - 1))
                    runLength = 0
                }

                if (index[pixelHash] == pixel) { // check for a run
                    buffer.write(QoiCodec.QOI_OP_INDEX or pixelHash)
                } else {
                    index[pixelHash] = pixel

                    if (previous.a == pixel.a) { // check if the pixel is applicable for a diff
                        val dr = (pixel.r - previous.r).toByte()
                        val dg = (pixel.g - previous.g).toByte()
                        val db = (pixel.b - previous.b).toByte()

                        if (tinyDifference(dr) && tinyDifference(dg) && tinyDifference(db)) {
                            buffer.write(QoiCodec.QOI_OP_DIFF or ((dr + 2) shl 4) or ((dg + 2) shl 2) or (db + 2))
                        } else {
                            val drg = dr - dg
                            val dbg = db - dg

                            if (smallerDifference(drg) && smallDifference(dg) && smallerDifference(dbg)) {
                                buffer.write(QoiCodec.QOI_OP_LUMA or (dg + 32))
                                buffer.write(((drg + 8) shl 4) or (dbg + 8))
                            } else {
                                buffer.write(image.QOI_PIXEL_PREFIX)
                                buffer.write(pixel.getBytes(image.channels))
                            }
                        }
                    } else { // if all else fails, write the full rgba value
                        buffer.write(image.QOI_PIXEL_PREFIX)
                        buffer.write(pixel.getBytes(image.channels))
                    }
                }
            }

            previous = pixel
        }

        buffer.write(QoiCodec.QOI_ENDF)
        buffer.close()
    }
}

/**
 * Small cli for the QoiEncoder which accepts an input and output file
 */
fun main(args: Array<String>) {
    if (args.size == 2) {
        val inputFile = File(args[0])
        val outputFile = File(args[1])
        outputFile.parentFile.mkdirs()

        if (inputFile.exists() and inputFile.isFile) {
            val encoder = QoiEncoder(QoiImage.fromFile(inputFile))
            encoder.writeFile(outputFile)
        } else println("$inputFile does not exist!")
    } else println("Not enough args (expected 2: input and output file)")
}
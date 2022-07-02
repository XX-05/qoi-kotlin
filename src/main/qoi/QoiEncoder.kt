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

                if (index[pixel.hash] == pixel) {
                    buffer.write(QOICodec.QOI_OP_INDEX or pixel.hash)
                } else {
                    index[pixel.hash] = pixel

                    if (previous.a == pixel.a) {
                        val dr = (pixel.r - previous.r).toByte()
                        val dg = (pixel.g - previous.g).toByte()
                        val db = (pixel.b - previous.b).toByte()

                        if (tinyDifference(dr) && tinyDifference(dg) && tinyDifference(db)) {
                            buffer.write(QOICodec.QOI_OP_DIFF or ((dr + 2) shl 4) or ((dg + 2) shl 2) or (db + 2))
                        } else {
                            val drg = dr - dg
                            val dbg = db - dg

                            if (smallerDifference(drg) && smallDifference(dg) && smallerDifference(dbg)) {
                                buffer.write(QOICodec.QOI_OP_LUMA or (dg + 32))
                                buffer.write(((drg + 8) shl 4) or (dbg + 8))
                            } else {
                                buffer.write(image.QOI_PIXEL_PREFIX)
                                buffer.write(pixel.bytes.slice(0 until image.channels).toByteArray()) // TODO: Fix this bs
                            }
                        }
                    } else {
                        buffer.write(image.QOI_PIXEL_PREFIX)
                        buffer.write(pixel.bytes.slice(0 until image.channels).toByteArray()) // TODO: Fix this bs
                    }
                }
            }

            previous = pixel
        }

        buffer.write(byteArrayOf(0, 0, 0, 0, 0, 0, 0, 1))
        buffer.close()
    }
}

fun main(args: Array<String>) {
    val testDir = File("assets/")

    for (file in testDir.listFiles()!!) {
        if (!file.name.endsWith("png")) continue

        val encoder = QoiEncoder(QoiImage.fromFile(file.path))
        encoder.writeFile(File("converted/${file.nameWithoutExtension}.qoi"))
    }
}
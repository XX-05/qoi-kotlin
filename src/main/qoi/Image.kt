package main.qoi

import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.io.File
import java.nio.ByteBuffer
import javax.imageio.ImageIO


/**
 * Color spaces that can be encoded in QOI.
 * The only two types of color spaces that accepted in a QOI header are:
 * sRGB with linear alpha or all channels linear
 */
enum class QoiColorSpace(val code: Byte) {
    sRGB(0x00),
    LINEAR(0x01)
}

/**
 * A basic image
 *
 * @param pixels: A 1d array of rgb pixel values going from the top-down left-to-right
 * @param width: The width of the image in pixels
 * @param height: The height of the image in pixels
 * @param colorSpace: The color space of the image -
 *                    i.e., either sRGB with linear alpha or all channels linear
 */
data class Image(val pixels: UByteArray, val width: Int, val height: Int, val channels: Int, val colorSpace: QoiColorSpace = QoiColorSpace.sRGB) {
    val size = pixels.size

    private fun getHeader(): ByteArray {
        val header = ByteBuffer.allocate(14)
        header.putInt(QOICodec.QOI_MAGIC)
        header.putInt(width)
        header.putInt(height)
        header.put(channels.toByte())
        header.put(colorSpace.code)

        return header.array()
    }

    val qoiHeader = getHeader()

    companion object Reader {
        /**
         * Loads an image from a file into a new Image object
         * @param imagePath: The absolute or relative path to the image file
         */
        fun fromFile(imagePath: String): Image {
            val image: BufferedImage = ImageIO.read(File(imagePath))
            val pixels: ByteArray = (image.raster.dataBuffer as DataBufferByte).data

            return Image(
                pixels = pixels.toUByteArray(),
                width = image.width, height = image.height,
                channels = image.sampleModel.numBands
            )
        }
    }
}
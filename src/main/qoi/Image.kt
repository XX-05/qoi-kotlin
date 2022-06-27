package main.qoi

import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.io.File
import javax.imageio.ImageIO


/**
 * Color spaces that can be encoded in QOI.
 * The only two types of color spaces that accepted in a QOI header are:
 * sRGB with linear alpha or all channels linear
 */
enum class QoiColorSpace(val code: UByte) {
    sRGB(1.toUByte()),
    LINEAR(1.toUByte())
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
data class Image(val pixels: UByteArray, val width: Int, val height: Int, val colorSpace: QoiColorSpace = QoiColorSpace.sRGB) {
    companion object Reader {
        /**
         * Loads an image from a file into a new Image object
         * @param imagePath: The absolute or relative path to the image file
         */
        fun fromFile(imagePath: String): Image {
            val image: BufferedImage = ImageIO.read(File(imagePath))
            val pixels: ByteArray = (image.raster.dataBuffer as DataBufferByte).data

            return Image(pixels.toUByteArray(), image.width, image.height)
        }
    }
}

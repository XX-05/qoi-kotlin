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
 * A single rgb(a) pixel of an image
 */
data class QoiPixel(val r: UByte, val g: UByte, val b: UByte, val a: UByte = 0xFF.toUByte()) {
    val hash = (r.toInt() * 3 + g.toInt() * 5 + b.toInt() * 7 + a.toInt() * 11) % 64
    val bytes = byteArrayOf(r.toByte(), g.toByte(), b.toByte(), a.toByte()) // TODO: Figure out how to make this only rgb when the image has no alpha

    operator fun minus(pixel: QoiPixel): QoiPixel {
        return QoiPixel(
            (r - pixel.r).toUByte(),
            (g - pixel.g).toUByte(),
            (b - pixel.b).toUByte(),
            (a - pixel.a).toUByte()
        )
    }
}

/**
 * A basic image
 *
 * @param pixels: A 1d UByteArray of rgb pixel values going from the top-down left-to-right
 * @param width: The width of the image in pixels
 * @param height: The height of the image in pixels
 * @param colorSpace: The color space of the image -
 *                    i.e., either sRGB with linear alpha or all channels linear
 */
data class QoiImage(val pixels: UByteArray, val width: Int, val height: Int, val channels: Int, val colorSpace: QoiColorSpace = QoiColorSpace.sRGB) {
    val size = pixels.size
    val QOI_PIXEL_PREFIX = if (channels == 4) QOICodec.QOI_OP_RGBA else QOICodec.QOI_OP_RGB

    /**
     * Returns the 14-byte QOI header containing information about the image
     */
    fun getQoiHeader(): ByteArray {
        val header = ByteBuffer.allocate(14)
        header.putInt(QOICodec.QOI_MAGIC)
        header.putInt(width)
        header.putInt(height)
        header.put(channels.toByte())
        header.put(colorSpace.code)

        return header.array()
    }

    /**
     * Returns a single RGB(A) pixel from the image
     *
     * @param index: The index of the image array that the pixel starts at
     */
    fun getPixel(index: Int): QoiPixel {
        if (size < index + channels)
            throw java.lang.IndexOutOfBoundsException("Not enough values in image to fill pixel: ${size - index}")

        return if (channels == 4) {
            val range = index..(index + 3)
            val (a, b, g, r) = pixels.slice(range)
            QoiPixel(r, g, b, a)
        } else {
            val (b, g, r) = pixels.slice(index..(index + 2))
            QoiPixel(r, g, b)
        }
    }

    companion object Reader {
        /**
         * Loads the pixels from a BufferedImage into a new Image object
         * @param image: The BufferedImage
         */
        fun fromBufferedImage(image: BufferedImage): QoiImage {
            val pixels: ByteArray = (image.raster.dataBuffer as DataBufferByte).data

            return QoiImage(
                pixels = pixels.toUByteArray(),
                width = image.width, height = image.height,
                channels = image.sampleModel.numBands
            )
        }

        /**
         * Loads an image from a file into a new Image object
         * @param imagePath: The absolute or relative path to the image file
         */
        fun fromFile(imagePath: String): QoiImage {
            val image: BufferedImage = ImageIO.read(File(imagePath))
            return fromBufferedImage(image)
        }
    }
}
package main.qoi

/**
 * Contains constants and utility methods for decoder and encoder.
 */
internal object QoiCodec {
    const val QOI_OP_INDEX = 0x00
    const val QOI_OP_DIFF = 0x40
    const val QOI_OP_LUMA = 0x80
    const val QOI_OP_RUN = 0xC0
    const val QOI_OP_RGB = 0xFE
    const val QOI_OP_RGBA = 0xFF
    const val QOI_MAGIC = ('q'.code shl 24) or ('o'.code shl 16) or ('i'.code shl 8) or 'f'.code
    val QOI_ENDF = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 1)
}

/**
 * Color spaces that can be encoded in QOI.
 * The only two types of color spaces that accepted in a QOI header are:
 * sRGB with linear alpha or all linear channels
 */
enum class QoiColorSpace(val code: Byte) {
    sRGB(0x00),
    LINEAR(0x01)
}
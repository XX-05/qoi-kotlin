package main.qoi

/**
 * Contains constants and utility methods for decoder and encoder.
 */
internal object QOICodec {
    const val QOI_SRGB = 0
    const val QOI_LINEAR = 1
    const val QOI_OP_INDEX = 0x00
    const val QOI_OP_DIFF = 0x40
    const val QOI_OP_LUMA = 0x80
    const val QOI_OP_RUN = 0xC0
    const val QOI_OP_RGB = 0xFE
    const val QOI_OP_RGBA = 0xFF
    const val QOI_MASK_2 = 0xC0
    const val QOI_MAGIC = 0x716f6966 // bytes for 'qoif'

    // Seven 0x00 bytes followed by 0x01
    val QOI_ENDF = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 1)
}
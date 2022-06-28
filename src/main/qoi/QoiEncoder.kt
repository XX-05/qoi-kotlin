package main.qoi

import java.io.File
import java.nio.ByteBuffer


private class BufferedQoiFileWriter(val file: File) {
    val buffer: ByteBuffer = ByteBuffer.allocate(8192)


    /**
     * Clears the contents of the buffer and writes it to the file.
     * This includes potentially null/unfilled array indexes
     * so the buffer should only be flushed to a file when it is completely full.
     */
    private fun flushToFile() {
        file.writeBytes(buffer.array())
        buffer.clear()
    }

    /**
     * Writes an array of bytes to the file
     *
     * @param bytes: The bytes to write
     */
    fun writeBytes(bytes: ByteArray) {
        if (!buffer.hasRemaining()) {
            flushToFile()
        }
        buffer.put(bytes)
    }

    /**
     * Writes the remaining content in the buffer to the file and clears the buffer.
     * This writer should be closed when the file no longer needs to be written to.
     * Otherwise, content could potentially remain in the buffer and end up not being written to the file.
     */
    fun close() {
        val remainingContent: ByteArray = buffer
            .array()
            .slice(0 until buffer.position())
            .toByteArray()
        file.writeBytes(remainingContent)
        buffer.clear()
    }
}

class QoiEncoder(private val image: Image) {
    /**
     * Writes the QOI-encoded image to a given file.
     *
     * @param file: The file to write the QOI image in
     */
    fun writeFile(file: File) {
        val buffer: BufferedQoiFileWriter = BufferedQoiFileWriter(file)
        buffer.writeBytes(image.qoiHeader)

        for (idx in 0..image.size - 3 step 3) {
            val (r, g, b) = image.pixels.slice(idx..(idx+3))
        }

        buffer.close()
    }
}

fun main(args: Array<String>) {
    val image = Image.fromFile("assets/testcard.png")
    val encoder = QoiEncoder(image)

    encoder.writeFile(File("testcard.qoi"))
}
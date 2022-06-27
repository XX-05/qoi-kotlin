package main.qoi

class QoiEncoder(private val image: Image) {
    fun encode(): ByteArray {
        val buffer: Array<Byte> = arrayOf(0x00.toByte())
        return buffer.toByteArray()
    }
}

fun main(args: Array<String>) {
    val image = Image.fromFile("assets/testcard.png")
    for (i in 20020..20023) {
        println(image.pixels[i])
    }
//    val enc = QoiEncoder(image)
//
//    enc.encode()
}
package com.koso.rx5.core.command.outgoing

class StaticMapCommand(
    val image: ByteArray
): BaseOutgoingCommand() {

    val length: Int
        get() {
            return image.size ?: 0
        }

    override fun value(): ByteArray {
        return concatenateByteArrays(
            length.toByteArray(4).reversedArray(),
            image)
    }

    override fun valueToString(): String {
        return "image_length=${length}"
    }

    override fun header1(): Byte {
        return 0xFF.toByte()
    }

    override fun header2(): Byte {
        return 0x88.toByte()
    }

    override fun end1(): Byte {
        return 0xFF.toByte()
    }

    override fun end2(): Byte {
        return 0x33.toByte()
    }
}
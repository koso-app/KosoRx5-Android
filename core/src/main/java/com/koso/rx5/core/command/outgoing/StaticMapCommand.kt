package com.koso.rx5.core.command.outgoing

class StaticMapCommand(
    val index: Int,
    val image: ByteArray
): BaseOutgoingCommand() {

    override fun value(): ByteArray {
        return image
    }

    override fun valueToString(): String {
        return "index_of_cmd=${index}"
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

    override fun encode(): ByteArray {
        val result = concatenateByteArrays(
            byteArrayOf(header1()),
            byteArrayOf(header2()),
            byteArrayOf(index.toByte()),
            value(),
            byteArrayOf(checkSum(value())),
            byteArrayOf(end1()),
            byteArrayOf(end2())
        )
        return result
    }

    companion object{
        fun createFromBytes(image: ByteArray): List<StaticMapCommand>{
            val result = mutableListOf<StaticMapCommand>()
            result.add(StaticMapCommand(0, image.size.toByteArray(4).reversedArray()))
            var index = 0
            while(index * 512 <= image.size){
                val endIndex = index + 1
                val start = index * 512
                val end = kotlin.math.min(endIndex * 512, image.size)
                val bytes = image.copyOfRange(start, end)

                result.add(StaticMapCommand(index + 1, bytes))
                index = endIndex
            }
            return result
        }
    }
}
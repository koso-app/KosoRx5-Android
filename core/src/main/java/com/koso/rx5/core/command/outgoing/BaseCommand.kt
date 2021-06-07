package com.koso.rx5.core.command.outgoing

import com.koso.rx5.core.util.Utility
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.StringBuilder
import java.util.*
import kotlin.experimental.xor

fun String.toByteArray(num: Int): ByteArray {
    val bytes = ByteArray(num)
    Arrays.fill(bytes, 0x00.toByte())
    for (i in this.indices) {
        if (i > bytes.size - 1) {
            break
        }

        bytes[i] = this[i].toByte()
    }
    return bytes
}

fun Int.toByteArray(num: Int): ByteArray {
    val bytes = ByteArray(num)
    for (i in (bytes.size - 1) downTo  0) {
        bytes[bytes.size - 1 - i] = (this shr 8 * i and 0xFF).toByte()
    }
    return bytes
}


abstract class BaseCommand {

    abstract fun value(): ByteArray

    abstract fun valueToString(): String

    abstract fun header1(): Byte

    abstract fun header2(): Byte

    abstract fun end1(): Byte

    abstract fun end2(): Byte

    private fun checkSum(array: ByteArray): Byte {
        var result = array[0]
        for (i in 1 until array.size) {
            result = result xor array[i]
        }
        return result
    }

    private fun totalLength(): Int {
        return value().size
    }

    fun encode(): ByteArray {
        var result = concatenateByteArrays(
            byteArrayOf(header1()),
            byteArrayOf(header2()),
            byteArrayOf(totalLength().toByte()),
            value(),
            byteArrayOf(checkSum(value())),
            byteArrayOf(end1()),
            byteArrayOf(end2())
        )
        return result
    }

    @Throws(IOException::class)
    fun concatenateByteArrays(vararg typeArrays: ByteArray): ByteArray {
        val output = ByteArrayOutputStream()
        println("-------------")
        for (ba in typeArrays) {
            output.write(ba)
            println(Utility.bytesToHex(ba))
        }
        return output.toByteArray()
    }

    fun concatenateByteArrays(bytes: List<ByteArray>): ByteArray{
        val output = ByteArrayOutputStream()
        println("-------------")
        for (ba in bytes) {
            output.write(ba)
            println(Utility.bytesToHex(ba))
        }
        return output.toByteArray()
    }

    override fun toString(): String {
        val builder = StringBuilder()
        builder.appendln("--------")
        builder.appendln("HEADER1 = ${Utility.bytesToHex(byteArrayOf(header1()))}")
        builder.appendln("HEADER2 = ${Utility.bytesToHex(byteArrayOf(header2()))}")
        builder.appendln("Length = ${Utility.bytesToHex(byteArrayOf(totalLength().toByte()))}")
        builder.appendln("Data = ${valueToString()}")
        builder.appendln("CheckSum = ${Utility.bytesToHex(byteArrayOf(checkSum(value())))}")
        builder.appendln("END1 = ${Utility.bytesToHex(byteArrayOf(end1()))}")
        builder.appendln("END2 = ${Utility.bytesToHex(byteArrayOf(end2()))}")
        builder.appendln("--------")
        return builder.toString()
    }
}
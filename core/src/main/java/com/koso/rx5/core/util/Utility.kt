package com.koso.rx5.core.util

import java.lang.IllegalStateException
import java.nio.ByteBuffer

object Utility {
    private val hexArray = "0123456789ABCDEF".toCharArray()
    fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v: Int = bytes[j].toInt() and 0xFF
            hexChars[j * 2] = hexArray[v ushr 4]
            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }

    fun stringToByteArrayWithSize(bytes: ByteArray, size: Int): ByteArray{

        val result = ByteArray(size)

        for (i in bytes.indices){
            result[i] = bytes[i]
        }
        return result
    }
}
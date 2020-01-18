package com.koso.rx5sample.utils

import com.koso.core.util.Utility
import java.math.BigInteger

class StringUtils {
    companion object{
        fun hexStringToByteArray(s: String): ByteArray {
            val len = s.length
            val data = ByteArray(len / 2)
            var i = 0
            BigInteger(s, 16).toByteArray()
            while (i < len) {
                data[i / 2] = ((Character.digit(s[i], 16) shl 4)
                        + Character.digit(s[i + 1], 16)).toByte()
                i += 2
            }
            return data
        }
        private val hexArray = "0123456789ABCDEF".toCharArray()
        fun byteArrayToHexString(bytes: ByteArray): String? {
            val hexChars = CharArray(bytes.size * 2)
            for (j in bytes.indices) {
                val v: Int = bytes[j].toInt() and 0xFF
                hexChars[j * 2] = hexArray[v ushr 4]
                hexChars[j * 2 + 1] = hexArray[v and 0x0F]
            }
            return String(hexChars)
        }
    }
}
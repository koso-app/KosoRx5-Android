package com.koso.rx5.core.util

fun String.decodeHex(): ByteArray {
    var text = this
    if(length % 2 != 0) { text = "0$this" }

    return text.chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
}

package com.koso.rx5.core.command.outgoing

import android.util.Log
import com.koso.rx5.core.util.Utility
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import kotlin.experimental.xor

abstract class BaseOutgoingKawasakiCommand {

    abstract fun payload(): ByteArray
    abstract fun header(): ByteArray



    open fun encode(): ByteArray {
        val result = concatenateByteArrays(
            header(),
            payload()
        )
        Log.d("xunqun", "payload: ${Utility.bytesToHex(result)}")
        return result
    }

    override fun toString(): String {
        val builder = StringBuilder()
        builder.appendLine("--------")
        builder.appendLine("HEADER = ${Utility.bytesToHex(header())}")
        builder.appendLine("PAYLOAD = ${Utility.bytesToHex(payload())}")
        builder.appendLine("--------")
        return builder.toString()
    }

    @Throws(IOException::class)
    fun concatenateByteArrays(vararg typeArrays: ByteArray): ByteArray {
        val output = ByteArrayOutputStream()
        for (ba in typeArrays) {
            output.write(ba)
        }
        return output.toByteArray()
    }

}
package com.koso.rx5.core.command.incoming

import com.koso.rx5.core.util.Utility

class KawasakiCommand: BaseIncomingCommand() {

    var title: String = "DATA SOURCE"
    var raw: ByteArray = byteArrayOf()
    var hexString: String = ""

    fun parseData(bytes: ByteArray){
        raw = bytes
        hexString = toString()
    }

    override fun parseData(rawData: MutableList<Byte>) {
        raw = rawData.toByteArray()
        hexString = toString()
    }

    override fun createInstance(): KawasakiCommand {
        return KawasakiCommand()
    }

    override fun toString(): String {
        return Utility.bytesToHex(raw)
    }

}
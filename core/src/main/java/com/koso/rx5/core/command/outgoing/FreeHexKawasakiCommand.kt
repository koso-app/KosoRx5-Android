package com.koso.rx5.core.command.outgoing

import com.koso.rx5.core.util.decodeHex

class FreeHexKawasakiCommand(val hexString: String) : BaseOutgoingKawasakiCommand() {
    override fun payload(): ByteArray {
        return hexString.decodeHex()
    }

    override fun header(): ByteArray {
        return byteArrayOf()
    }

}
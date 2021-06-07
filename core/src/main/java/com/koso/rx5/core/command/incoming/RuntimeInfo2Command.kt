package com.koso.rx5.core.command.incoming

class RuntimeInfo2Command : BaseIncomingCommand() {

    override fun header1(): Byte {
        return 0xFF.toByte()
    }

    override fun header2(): Byte {
        return 0x80.toByte()
    }

    override fun end1(): Byte {
        return 0xFF.toByte()
    }

    override fun end2(): Byte {
        return 0x2C.toByte()
    }

    override fun createInstance(): RuntimeInfo2Command {
        return RuntimeInfo2Command()
    }

    fun parseRawData() {
        val length = rawData.size
    }

}
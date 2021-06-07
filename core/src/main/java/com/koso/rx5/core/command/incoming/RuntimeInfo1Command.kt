package com.koso.rx5.core.command.incoming

class RuntimeInfo1Command: BaseIncomingCommand() {

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
        return 0x2B.toByte()
    }

    override fun createInstance(): RuntimeInfo1Command {
        return RuntimeInfo1Command()
    }

    fun parseRawData(){
        val length = rawData.size
    }

}
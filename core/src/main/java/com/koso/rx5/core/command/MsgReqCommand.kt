package com.koso.rx5.core.command

import java.lang.IllegalStateException

class MsgReqCommand(val cmd: Int, val pollingList: List<PollingItem>): BaseCommand() {
    inner class ClearCommandBuilder {
        fun build(): MsgReqCommand{
            return MsgReqCommand(0x00, listOf())
        }
    }

    inner class ReqCommandBuilder{
        private val list = ArrayList<PollingItem>()
        fun addItem(item: PollingItem): ReqCommandBuilder{
            list.add(item)
            return this
        }

        fun build(): MsgReqCommand{
            if(list.size > 0) {
                return MsgReqCommand(0x80, list)
            }else{
                throw IllegalStateException("No any polling item added in the request list")
            }
        }
    }

    class PollingItem(msgId: Int, hz: Int)

    override fun value(): ByteArray {
        return byteArrayOf()
    }

    override fun valueToString(): String {
        return ""
    }

    override fun header1(): Byte {
        return 0xFF.toByte()
    }

    override fun header2(): Byte {
        return 0x60.toByte()
    }

    override fun end1(): Byte {
        return 0xFF.toByte()
    }

    override fun end2(): Byte {
        return 0x0B.toByte()
    }

}
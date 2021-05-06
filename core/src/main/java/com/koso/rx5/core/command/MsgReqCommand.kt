package com.koso.rx5.core.command

import com.koso.rx5.core.util.Utility
import java.io.ByteArrayOutputStream
import java.lang.IllegalStateException

class MsgReqCommand(val cmd: Int, val pollingList: List<PollingItem>): BaseCommand() {
    class ClearCommandBuilder {
        fun build(): MsgReqCommand{
            return MsgReqCommand(0, listOf())
        }
    }

    class ReqCommandBuilder{
        private val list = ArrayList<PollingItem>()
        fun addItem(item: PollingItem): ReqCommandBuilder{
            list.add(item)
            return this
        }

        fun build(): MsgReqCommand{
            if(list.size > 0) {
                return MsgReqCommand(128, list)
            }else{
                throw IllegalStateException("No any polling item added in the request list")
            }
        }
    }

    class PollingItem(val msgId: Int, val hz: Int)

    override fun value(): ByteArray {
        val output = ByteArrayOutputStream()
        output.write(getCmdId())
        for (item in getItems()){
            output.write(item)
        }
        return output.toByteArray()
    }

    fun getCmdId(): ByteArray{
        return cmd.toByteArray(4).reversedArray()
    }

    fun getItems(): List<ByteArray>{
        val result = arrayListOf<ByteArray>()
        for (i in pollingList.indices){
            val index = i * 2
            result.add(pollingList[i].msgId.toByteArray(4).reversedArray())
            result.add(pollingList[i].hz.toByteArray(4).reversedArray())
        }
        return result
    }

    override fun valueToString(): String {
        var items = ""
        for(i in pollingList.indices){
            items += Utility.bytesToHex(pollingList[i].msgId.toByteArray(4).reversedArray()) + ","
            items += Utility.bytesToHex(pollingList[i].hz.toByteArray(4).reversedArray()) + ","

        }
        return "{" +
                "id: ${Utility.bytesToHex(getCmdId())}," +
                "$items" +
                "}"
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
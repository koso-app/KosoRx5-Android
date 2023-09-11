package com.koso.rx5.core.command.outgoing

import android.util.Log
import com.koso.rx5.core.util.Utility.bytesToHex


class NaviInfoKawasakiCommand(
    val mode: Int, //0:Under Navigation, 1:Under Routing, 2:Completed, 3:No Navigation
    val seqnum: Int,
    val turndistance: Int,
    val distanceunit: Int, //0:km, 1:m, 2:mile, 3:ft, 4:yd, 5-15:RESERVE
    val turntype: Int,          //int
) : BaseOutgoingKawasakiCommand() {
    val bid:Int = 0x14


    override fun header(): ByteArray {
        val payload = payload()
        return concatenateByteArrays(
            0x14.toByteArray(1),
            payload.size.toByteArray(1),
            seqnum.toByteArray(1),
        )
    }

    /**
     * Byte data in the payload is using Big-endian order
     */
    override fun payload(): ByteArray {
        val result = concatenateByteArrays(
            byteArrayOf(0xff.toByte(), 0xff.toByte()),
            byteArrayOf(0x05, 0x70),
            get570(),
            byteArrayOf(0x05, 0x71),
            get571(),
            byteArrayOf(0x05, 0x72),
            get572()
        )

        return result
    }

    private fun get570(): ByteArray {
        var modeByte = when(mode){
            1 -> byteArrayOf(0x1f)
            2 -> byteArrayOf(0x2f)
            3 -> byteArrayOf(0x3f)
            else -> byteArrayOf(0x0f)
        }
        return concatenateByteArrays(
            modeByte,
            ByteArray(7) { i -> 0xff.toByte() }
        )
    }

    private fun get571(): ByteArray {
        val unitBytes = distanceunit or 0xF0
        return concatenateByteArrays(
            turntype.toByteArray(1),
            unitBytes.toByteArray(1),
            turndistance.toByteArray(3),
            ByteArray(3) { i -> 0xff.toByte() }
        )
    }

    private fun get572(): ByteArray {
        return ByteArray(8) {i -> 0xff.toByte()}
    }

}
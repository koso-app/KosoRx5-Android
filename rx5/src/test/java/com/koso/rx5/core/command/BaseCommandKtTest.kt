package com.koso.rx5.core.command

import com.koso.rx5.core.util.Utility
import org.junit.Test

class BaseCommandKtTest{

    @ExperimentalStdlibApi
    @Test
    fun testToByteArray(){
        println(Utility.bytesToHex(1800.toByteArray(4)))
        assert(Utility.bytesToHex(1800.toByteArray(4)) == "00000708")

        println(Utility.bytesToHex("中華路".encodeToByteArray()))
    }


}
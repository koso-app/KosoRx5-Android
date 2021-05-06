package com.koso.rx5.core.command

import com.koso.rx5.core.util.Utility
import org.junit.Test

class BaseCommandKtTest{

    @ExperimentalStdlibApi
    @Test
    fun testToByteArray(){
        println(Utility.bytesToHex(128.toByteArray(4).reversedArray()))

    }


}
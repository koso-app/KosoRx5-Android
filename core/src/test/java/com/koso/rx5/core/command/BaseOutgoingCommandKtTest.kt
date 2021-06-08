package com.koso.rx5.core.command

import com.koso.rx5.core.command.outgoing.toByteArray
import org.junit.Test

class BaseOutgoingCommandKtTest{

    @ExperimentalStdlibApi
    @Test
    fun testToByteArray(){
        val text = 123
        val array = text.toByteArray(4)
        println(array)
        println(String(array))
    }
}
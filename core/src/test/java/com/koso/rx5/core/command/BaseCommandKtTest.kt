package com.koso.rx5.core.command

import com.koso.rx5.core.util.Utility
import org.junit.Test

class BaseCommandKtTest{

    @ExperimentalStdlibApi
    @Test
    fun testToByteArray(){
        val text = 123
        val array = text.toByteArray(4)
        println(array)
        println(String(array))
    }
}
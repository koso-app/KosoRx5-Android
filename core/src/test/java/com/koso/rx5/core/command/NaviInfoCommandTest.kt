package com.koso.rx5.core.command

import com.koso.rx5.core.util.Utility
import org.junit.Test

class NaviInfoCommandTest{

    @Test
    fun encodeTest(){
        val content = "台南市".toByteArray()
        val result = Utility.stringToByteArrayWithSize(content, 24)

        System.out.println(String(result, Charsets.UTF_8))
        System.out.println(Utility.bytesToHex(result))
        System.out.println(result.toString(Charsets.UTF_8))



    }
}
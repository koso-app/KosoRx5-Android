package com.koso.rx5.core.command

import com.koso.rx5.core.util.Utility
import org.junit.Assert.*
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset

class NaviInfoCommandTest{

    @Test
    fun encodeTest(){
        var result = ByteArray(24)
        val content = "abcd".toByteArray()
        for (i in content.indices){
            result[i] = content[i]
        }

        System.out.println(String(result, Charsets.UTF_8))
        System.out.println(Utility.bytesToHex(result))
        System.out.println(Utility.bytesToHex(result.reversedArray()))
        System.out.println(result.toString(Charsets.UTF_8))


        val buffer = ByteBuffer.allocate(24)
        buffer.put(content)
        System.out.println(Utility.bytesToHex(buffer.order(ByteOrder.BIG_ENDIAN).array().reversedArray()))
    }
}
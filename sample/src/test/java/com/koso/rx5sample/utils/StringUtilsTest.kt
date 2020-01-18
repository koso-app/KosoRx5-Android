package com.koso.rx5sample.utils

import org.junit.Test

class StringUtilsTest{
    @Test
    fun testHexStringToByteArray(){
        val bytes = StringUtils.hexStringToByteArray("FFFD")
        System.out.println("${bytes[0]}")
        val bytes1 = byteArrayOf(0xFF.toByte(), 0xFD.toByte())
        System.out.println("${bytes1[0]}")
        val hex = StringUtils.byteArrayToHexString(bytes1)
        System.out.println(hex)
    }
}
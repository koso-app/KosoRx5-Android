package com.koso.core.util

import org.junit.Test

class UtilityTest {

    @Test
    fun testBytesToHex(){
        val hexstring = Utility.bytesToHex(byteArrayOf(0xFF.toByte()))
        System.out.println("$hexstring -> ${0xFF})")
    }
}
package com.koso.rx5.core.command.incoming

import kotlin.experimental.xor
import kotlin.reflect.KClass

abstract class BaseIncomingCommand {
    protected var length = 0
    protected var rawData = byteArrayOf()

    abstract fun header1(): Byte
    abstract fun header2(): Byte
    abstract fun end1(): Byte
    abstract fun end2(): Byte

    abstract fun createInstance(): BaseIncomingCommand?

    fun create(buffer: MutableList<Byte>): BaseIncomingCommand?{
        val instance = createInstance()
        val feasible = instance?.loadBuffer(buffer)
        return if(feasible == true) instance else null
    }

    fun loadBuffer(buffer: MutableList<Byte>): Boolean{
        length = buffer[2].toInt()
        rawData = ByteArray(length)
        for(i in 0 until length){
            rawData[i] = buffer[0 + 3]
        }
        val checkSum = checkSum(rawData)
//        return checkSum == buffer[3 + length]
        return true
    }

    private fun checkSum(array: ByteArray): Byte {
        var result = array[0]
        for (i in 1 until array.size) {
            result = result xor array[i]
        }
        return result
    }
}

enum class AvailableIncomingCommands(
    val header1: Byte,
    val header2: Byte,
    val end1: Byte,
    val end2: Byte,
    val classObject: Class<out BaseIncomingCommand>
) {
    RuntimeInfo1(0xFF.toByte(), 0x80.toByte(), 0xFF.toByte(), 0x2B, RuntimeInfo1Command::class.java),
    RuntimeInfo2(0xFF.toByte(), 0x81.toByte(), 0xFF.toByte(), 0x2C, RuntimeInfo2Command::class.java)
}
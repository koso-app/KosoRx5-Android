package com.koso.rx5.core.command.incoming

import kotlin.experimental.xor

abstract class BaseIncomingCommand {

    protected var length = 0
    protected var rawData = mutableListOf<Byte>()

    abstract fun parseData(rawData: MutableList<Byte>)
    abstract fun createInstance(): BaseIncomingCommand?


    fun create(buffer: MutableList<Byte>): BaseIncomingCommand? {
        val instance = createInstance()
        val feasible = instance?.loadBuffer(buffer)
        return if (feasible == true) instance else null
    }

    fun loadBuffer(buffer: MutableList<Byte>): Boolean {
        length = buffer[2].toInt()
        rawData = mutableListOf()
        for (i in 0 until length) {
            rawData.add(buffer[i + 3])
        }
        parseData(rawData)
        val checkSum = checkSum(rawData)
//        return checkSum == buffer[3 + length]
        return true
    }

    private fun checkSum(array: MutableList<Byte>): Byte {
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
    RuntimeInfo1(
        0xFF.toByte(),
        0x80.toByte(),
        0xFF.toByte(),
        0x2B,
        RuntimeInfo1Command::class.java
    ),
    RuntimeInfo2(0xFF.toByte(), 0x81.toByte(), 0xFF.toByte(), 0x2C, RuntimeInfo2Command::class.java)
}
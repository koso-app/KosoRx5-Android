package com.koso.rx5.core.command

import com.koso.rx5.core.command.outgoing.NaviInfoCommand
import com.koso.rx5.core.command.outgoing.toByteArray
import com.koso.rx5.core.util.Utility
import org.junit.Test

class CommandTest {

    @ExperimentalStdlibApi
    @Test
    fun testNaviInfoCommand() {
        val cmd =
            NaviInfoCommand(0, "台南市東區", "中華路", "17號", 40, 25, 40, 40, 25, "西門路", 350, 3, 10, 35000, 40, 12, 120)


        println("size = ${cmd.value().size}")
        assert(cmd.value().size == 212)

        val code = Utility.bytesToHex(1.toByteArray(4))
        println("??? $code")
    }
}
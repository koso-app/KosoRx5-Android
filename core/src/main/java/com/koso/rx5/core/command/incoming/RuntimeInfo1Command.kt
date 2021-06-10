package com.koso.rx5.core.command.incoming

import android.util.Log
import java.lang.Exception
import java.nio.ByteBuffer

class RuntimeInfo1Command: BaseIncomingCommand() {

    var speed = 0 //km/h
    var rpm = 0 //轉速r/min
    var batt_vc = 0 // 電瓶電壓 (單位 0.1V)
    var consume = 0 //目前油耗 L/H
    var gear = 0 //檔位
    var fuel = 0   // Fuel Level (格數:bit0~6 , 警告bit7)

    override fun parseData(rawData: MutableList<Byte>) {
        try {
            speed = ByteBuffer.allocate(2).apply {
                put(rawData[1])
                put(rawData[0])
            }.getShort(0).toInt()

            rpm = ByteBuffer.allocate(2).apply {
                put(rawData[3])
                put(rawData[2])
            }.getShort(0).toInt()

            batt_vc = ByteBuffer.allocate(2).apply {
                put(rawData[5])
                put(rawData[4])
            }.getShort(0).toInt()

            consume = ByteBuffer.allocate(2).apply {
                put(rawData[7])
                put(rawData[6])
            }.getShort(0).toInt()

            gear = rawData[8].toInt()

            fuel = rawData[9].toInt()

        }catch (e: Exception){
            e.printStackTrace()
        }
        Log.d("rx5", toString())
    }

    override fun createInstance(): RuntimeInfo1Command {
        return RuntimeInfo1Command()
    }

    override fun toString(): String {
        return "RuntimeInfo1Command{speed=$speed, rpm=$rpm, gear=$gear, batt_vc=$batt_vc, fuel=$fuel, consume=$consume} "
    }

}
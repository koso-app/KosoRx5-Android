package com.koso.rx5.core.command.incoming

import android.util.Log
import java.nio.ByteBuffer

/**
 * Reference to the online document: https://docs.google.com/spreadsheets/d/1r5r2UVenvF9o7I0D8ooWuyL1JjUIUd2M/edit#gid=905672726
 */
class RuntimeInfo2Command : BaseIncomingCommand() {

    var odo = 0 // 32bit
    var odo_total = 0 // 32bits
    var average_speed = 0 //kmh, 32bits
    var rd_time = 0 //總騎乘時間 sec, 16bit
    var average_consume = 0 //平均油耗L/H, 16 bits
    var trip_1 = 0 //小里程數 m, 32bits
    var trip_1_time = 0 //單位sec, 32bits
    var trip_1_average_speed = 0 //16bits
    var trip_1_average_consume = 0 //16bits

    var trip_2 = 0 //小里程數 m, 32bits
    var trip_2_time = 0 //單位sec, 32bits
    var trip_2_average_speed = 0 //16bits
    var trip_2_average_consume = 0 //16bits

    var trip_a = 0 //自動騎乘時間, 32bits
    var al_time = 0 //總時間紀錄, 32bits
    var fuel_range = 0 //km, 32bits
    var service_DST = 0 //剩餘保養里程, 32bits



    override fun parseData(rawData: MutableList<Byte>) {
        Log.d("rx5", "length: $length")
        odo = ByteBuffer.allocate(4).apply {
            put(rawData[0])
            put(rawData[1])
            put(rawData[2])
            put(rawData[3])
        }.getInt()

        odo_total = ByteBuffer.allocate(4).apply {
            put(rawData[4])
            put(rawData[5])
            put(rawData[6])
            put(rawData[7])
        }.getInt()

        rd_time = ByteBuffer.allocate(4).apply {
            put(rawData[8])
            put(rawData[9])
            put(rawData[10])
            put(rawData[11])
        }.getInt()

        average_speed = ByteBuffer.allocate(2).apply {
            put(rawData[12])
            put(rawData[13])
        }.getShort().toInt()

        average_consume = ByteBuffer.allocate(2).apply {
            put(rawData[14])
            put(rawData[15])
        }.getShort().toInt()

        trip_1 = ByteBuffer.allocate(4).apply {
            put(rawData[16])
            put(rawData[17])
            put(rawData[18])
            put(rawData[19])
        }.getInt()

        trip_1_time = ByteBuffer.allocate(4).apply {
            put(rawData[20])
            put(rawData[21])
            put(rawData[22])
            put(rawData[23])
        }.getInt()

        trip_1_average_speed = ByteBuffer.allocate(2).apply {
            put(rawData[24])
            put(rawData[25])
        }.getShort().toInt()

        trip_1_average_consume = ByteBuffer.allocate(2).apply {
            put(rawData[26])
            put(rawData[27])
        }.getShort().toInt()

        trip_2 = ByteBuffer.allocate(4).apply {
            put(rawData[28])
            put(rawData[29])
            put(rawData[30])
            put(rawData[31])
        }.getInt()

        trip_2_time = ByteBuffer.allocate(4).apply {
            put(rawData[32])
            put(rawData[33])
            put(rawData[34])
            put(rawData[35])
        }.getInt()

        trip_2_average_speed = ByteBuffer.allocate(2).apply {
            put(rawData[36])
            put(rawData[37])
        }.getShort().toInt()

        trip_2_average_consume = ByteBuffer.allocate(2).apply {
            put(rawData[38])
            put(rawData[39])
        }.getShort().toInt()

        trip_a = ByteBuffer.allocate(4).apply {
            put(rawData[40])
            put(rawData[41])
            put(rawData[42])
            put(rawData[43])
        }.getInt()

        al_time = ByteBuffer.allocate(4).apply {
            put(rawData[44])
            put(rawData[45])
            put(rawData[46])
            put(rawData[47])
        }.getInt()

        service_DST = ByteBuffer.allocate(4).apply {
            put(rawData[48])
            put(rawData[49])
            put(rawData[50])
            put(rawData[51])
        }.getInt()

        fuel_range = ByteBuffer.allocate(4).apply {
            put(rawData[52])
            put(rawData[53])
            put(rawData[54])
            put(rawData[55])
        }.getInt()
    }

    override fun createInstance(): RuntimeInfo2Command {
        return RuntimeInfo2Command()
    }

}
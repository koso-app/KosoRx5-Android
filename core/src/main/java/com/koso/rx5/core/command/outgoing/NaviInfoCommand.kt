package com.koso.rx5.core.command.outgoing

import com.koso.rx5.core.util.Utility


class NaviInfoCommand(
    val navimode: Int,          //navimode  =0導航模式,=1導航模擬,=2瀏覽模式,=3導航模擬暫停瀏覽模式,=4導航暫時瀏覽模式
    val ctname: String,      //char         ctname[24]; 縣市行政區   // /*strcpy( an.ctname, "新北市三重區" );*/ strcpy( an.nowroadname, "\xa5\xfa\xb4\x5f\xb8\xf4\xa4\x40\xac\x71" ); //Big5編碼
    val roadname: String,    //char         nowroadname[64];  //目前道路名稱// /*strcpy( an.nowroadname, "光復路一段" );*/ strcpy( an.ctname, "\xb7\x73\xa5\x5f\xa5\xab\xa4\x54\xad\xab\xb0\xcf" );  //Big5編碼
    val doornum: String,     //char         doornum[24]; // 數字字碼或中文字碼
    val limitsp: Int,           //int          limitsp;  //目前道路速限 單位 : 公里  an.limitsp = 60;  //-1表示無速限資料
    val nextroadname: String,//char         nextroadname[64];  //下條轉彎道路名稱 /*strcpy( an.nextroadname, "重新路一段" );*/ strcpy( an.nextroadname, "\xad\xab\xb7\x73\xb8\xf4\xa4\x40\xac\x71" );  //Big5編碼
    val nextdist: Int,          //int          nextdist;  //下條轉彎道路距離 單位 : 公尺   an.nextdist = 200;
    val nextturn: Int,          //int          nextturn; //下條轉彎方向i=nextturn%100,道路型態bbb=(int)(nextturn/100) an.nextdist = 0;(直行),an.nextdist = 100;(橋梁)
    val camera: Int,            //int          camera; //測試照相警示 : 前方是否有測試照相, 0表無測速照相,1表有測速照相,大於1表測試照相距離  an.camera = 0;
    val navidist: Int,          //int          navidist; //路徑距離總長 單位:公尺        an.navidist = 35004;
    val navitime: Int,          //int          navitime; //導航所需時間 單位:分          an.navitime = 30;
    val gpsnum: Int,            //int          gpsnum; //GPS 可用衛星(ACTIVE)數          an.gpsnum = 12;
    val gpsdir: Int
) : BaseOutgoingCommand() {
    override fun value(): ByteArray {

        return concatByteArrays(
            getNaviMode(),
            getCtName(),
            getRoadName(),
            getDoorNum(),
            getLimitSpeed(),
            getNextRoadName(),
            getNextDist(),
            getNextTurn(),
            getCamera(),
            getNaviDist(),
            getNaviTime(),
            getGpsNum(),
            getGpsDir()
        )
    }

    fun getNaviMode(): ByteArray {
        return navimode.toByteArray(4).reversedArray()
    }

    fun getCtName(): ByteArray {
        return Utility.stringToByteArrayWithSize(ctname.toByteArray(), 24)
    }

    fun getRoadName(): ByteArray {
        return Utility.stringToByteArrayWithSize(roadname.toByteArray(), 64)
    }

    fun getDoorNum(): ByteArray {
        return Utility.stringToByteArrayWithSize(doornum.toByteArray(), 24)
    }

    fun getLimitSpeed(): ByteArray {
        return limitsp.toByteArray(4).reversedArray()
    }

    fun getNextRoadName(): ByteArray {
        return Utility.stringToByteArrayWithSize(nextroadname.toByteArray(), 64)
    }

    fun getNextDist(): ByteArray {
        return nextdist.toByteArray(4).reversedArray()
    }

    fun getNextTurn(): ByteArray {
        return nextturn.toByteArray(4).reversedArray()
    }

    fun getCamera(): ByteArray {
        return camera.toByteArray(4).reversedArray()
    }

    fun getNaviDist(): ByteArray {
        return navidist.toByteArray(4).reversedArray()
    }

    fun getNaviTime(): ByteArray {
        return navitime.toByteArray(4).reversedArray()
    }

    fun getGpsNum(): ByteArray {
        return gpsnum.toByteArray(4).reversedArray()
    }

    fun getGpsDir(): ByteArray {
        return gpsdir.toByteArray(4).reversedArray()
    }

    override fun valueToString(): String {
        val builder = StringBuilder()
        builder.appendLine("{")
        builder.appendLine("NaviMode = ${Utility.bytesToHex(getNaviMode())}")
        builder.appendLine("CityName = ${Utility.bytesToHex(getCtName())}")
        builder.appendLine("RoadName = ${Utility.bytesToHex(getRoadName())}")
        builder.appendLine("DoorNum = ${Utility.bytesToHex(getDoorNum())}")
        builder.appendLine("LimitSpeed = ${Utility.bytesToHex(getLimitSpeed())}")
        builder.appendLine(
            "NextRoadName = ${
                Utility.bytesToHex(
                    getNextRoadName()
                )
            }"
        )
        builder.appendLine("NextDist = ${Utility.bytesToHex(getNextDist())}")
        builder.appendLine("NextTurn = ${Utility.bytesToHex(getNextTurn())}")
        builder.appendLine("Camera = ${Utility.bytesToHex(getCamera())}")
        builder.appendLine("NaviDist = ${Utility.bytesToHex(getNaviDist())}")
        builder.appendLine("NaviTime = ${Utility.bytesToHex(getNaviTime())}")
        builder.appendLine("GpsNum = ${Utility.bytesToHex(getGpsNum())}")
        builder.appendLine("GpsDir = ${Utility.bytesToHex(getGpsDir())}")
        builder.appendLine("}")

        return builder.toString()
    }

    override fun header1(): Byte {
        return 0xFF.toByte()
    }

    override fun header2(): Byte {
        return 0x89.toByte()
    }

    override fun end1(): Byte {
        return 0xFF.toByte()
    }

    override fun end2(): Byte {
        return 0x34.toByte()
    }
}
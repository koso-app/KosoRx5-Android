package com.koso.core.command

import com.koso.core.util.Utility
import java.nio.charset.Charset


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

) : BaseCommand() {
    override fun value(): ByteArray {

        return concatenateByteArrays(
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
        return navimode.toByteArray(4)
    }

    fun getCtName(): ByteArray {
        return String(
            ctname.toByteArray(Charset.forName("UTF-8")),
            Charset.forName("BIG5")
        ).toByteArray(24)
    }

    fun getRoadName(): ByteArray {
        return String(
            roadname.toByteArray(Charset.forName("UTF-8")),
            Charset.forName("BIG5")
        ).toByteArray(64)
    }

    fun getDoorNum(): ByteArray {
        return String(
            doornum.toByteArray(Charset.forName("UTF-8")),
            Charset.forName("BIG5")
        ).toByteArray(24)
    }

    fun getLimitSpeed(): ByteArray {
        return limitsp.toByteArray(4)
    }

    fun getNextRoadName(): ByteArray {
        return String(
            nextroadname.toByteArray(Charset.forName("UTF-8")),
            Charset.forName("BIG5")
        ).toByteArray(64)
    }

    fun getNextDist(): ByteArray {
        return nextdist.toByteArray(4)
    }

    fun getNextTurn(): ByteArray {
        return nextturn.toByteArray(4)
    }

    fun getCamera(): ByteArray {
        return camera.toByteArray(4)
    }

    fun getNaviDist(): ByteArray {
        return navidist.toByteArray(4)
    }

    fun getNaviTime(): ByteArray {
        return navitime.toByteArray(4)
    }

    fun getGpsNum(): ByteArray {
        return gpsnum.toByteArray(4)
    }

    fun getGpsDir(): ByteArray {
        return gpsdir.toByteArray(4)
    }

    override fun valueToString(): String {
        val builder = StringBuilder()
        builder.appendln("{")
        builder.appendln("NaviMode = ${Utility.bytesToHex(getNaviMode())}")
        builder.appendln("CityName = ${Utility.bytesToHex(getCtName())}")
        builder.appendln("RoadName = ${Utility.bytesToHex(getRoadName())}")
        builder.appendln("DoorNum = ${Utility.bytesToHex(getDoorNum())}")
        builder.appendln("LimitSpeed = ${Utility.bytesToHex(getLimitSpeed())}")
        builder.appendln(
            "NextRoadName = ${Utility.bytesToHex(
                getNextRoadName()
            )}"
        )
        builder.appendln("NextDist = ${Utility.bytesToHex(getNextDist())}")
        builder.appendln("NextTurn = ${Utility.bytesToHex(getNextTurn())}")
        builder.appendln("Camera = ${Utility.bytesToHex(getCamera())}")
        builder.appendln("NaviDist = ${Utility.bytesToHex(getNaviDist())}")
        builder.appendln("NaviTime = ${Utility.bytesToHex(getNaviTime())}")
        builder.appendln("GpsNum = ${Utility.bytesToHex(getGpsNum())}")
        builder.appendln("GpsDir = ${Utility.bytesToHex(getGpsDir())}")
        builder.appendln("}")


        return builder.toString()
    }

}
package com.koso.core.command


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
            navimode.toByteArray(4),
            ctname.toByteArray(24),
            roadname.toByteArray(64),
            doornum.toByteArray(24),
            limitsp.toByteArray(4),
            nextroadname.toByteArray(64),
            nextdist.toByteArray(4),
            nextturn.toByteArray(4),
            camera.toByteArray(4),
            navidist.toByteArray(4),
            navitime.toByteArray(4),
            gpsnum.toByteArray(4),
            gpsdir.toByteArray(4)
        )
    }

}
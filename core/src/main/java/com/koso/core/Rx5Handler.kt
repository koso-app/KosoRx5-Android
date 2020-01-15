package com.koso.core

import android.content.Context

object Rx5Handler{


    /**
     * Observable connection state
     */
    val stateLiveData = BaseBluetoothDevice.stateLive

    var rx5: BaseBluetoothDevice? = null

    /**
     * Start connection service
     */
    fun startConnectService(context: Context){
        ConnectionService.startService(context)
    }

    /**
     * Stop connection service
     */
    fun stopConnectService(context: Context){
        ConnectionService.stopService(context)
    }

}
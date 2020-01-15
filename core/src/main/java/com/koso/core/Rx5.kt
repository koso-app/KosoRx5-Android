package com.koso.core

import android.content.Context
import android.content.Intent

class Rx5 private constructor(context: Context) : BaseBluetoothDevice(context, "00001101-0000-1000-8000-00805F9B34FB") {
    companion object {
        var instance: Rx5? = null
        fun instantiation(context: Context): Rx5 {
            if (instance == null) {
                instance = Rx5(context)
            }
            return instance!!
        }

        fun connect(context: Context){
            ConnectionService.startService(context)
        }

        fun disconnect(context: Context){
            ConnectionService.stopService(context)
        }


    }
}
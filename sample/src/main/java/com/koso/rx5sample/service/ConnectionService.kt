package com.koso.rx5sample.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.koso.core.Rx5


class ConnectionService : Service() {
    /**
     * Binder given to clients
     */
    private val binder: IBinder = ConnectServiceBinder()

    /**
     * The connection manager
     */
    val rx5 = Rx5(this)

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class ConnectServiceBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        val service: ConnectionService = this@ConnectionService
    }

    override fun onDestroy() {
        super.onDestroy()
        rx5.destory()
    }

}

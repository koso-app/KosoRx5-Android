package com.koso.core

import android.annotation.TargetApi
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Binder
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer


class ConnectionService : LifecycleService() {


    companion object{
        private val EXTRA_STOP = "extra_stop"

        fun startService(context: Context){
            context.startService(Intent(context, ConnectionService::class.java))
        }

        fun stopService(context: Context){
            val intent = Intent(context, ConnectionService::class.java)
            intent.putExtra(EXTRA_STOP, true)
            context.startService(intent)
        }
    }


    /**
     * Channel id to build Notification
     */
    private val CHANNEL_ID = "RX5 Connection"
    /**
     * The id parameter for startForground()
     */
    private val ONGOING_NOTIFICATION_ID = 13



    private val connectionStateObserver = Observer<BaseBluetoothDevice.State> {
        when (it) {
            BaseBluetoothDevice.State.Disconnected -> {
                stopSelf()
            }
            BaseBluetoothDevice.State.Connected -> {
                postOngoingNotification()
            }
            BaseBluetoothDevice.State.Connecting -> {
            }
            BaseBluetoothDevice.State.Discovering -> {

            }
            else -> {

            }
        }
    }
    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class ConnectServiceBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        val service: ConnectionService = this@ConnectionService
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if(intent != null && intent.getBooleanExtra(EXTRA_STOP, false)){
            stopSelf()
        }else {

            if (Rx5Handler.rx5 == null) {
                Rx5Handler.rx5 = BaseBluetoothDevice(this)

            }

            if (Rx5Handler.stateLive.value == BaseBluetoothDevice.State.Disconnected) {
                Rx5Handler.rx5!!.connectAsServer()
                registerConnectionState()
            }
        }

        return START_NOT_STICKY
    }

    private fun registerConnectionState() {
        Rx5Handler.stateLive.observe(this, connectionStateObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
        Rx5Handler.destory()
    }

    private fun postOngoingNotification() {
        val pendingIntent: PendingIntent =
            Intent("com.koso.rx5.action.VIEW").let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }


        val notification: Notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val cId = createNotificationChannel("msda", CHANNEL_ID)
            NotificationCompat.Builder(this, cId)
                .setContentTitle("RX5")
                .setContentText("Connection service started")
                .setSmallIcon(R.drawable.ic_stat_connect)
                .setContentIntent(pendingIntent)
                .setTicker("Connection service started")
                .build()
        } else {
            getNotificationBuilder(
                this,
                "",  // Channel id
                NotificationManagerCompat.IMPORTANCE_DEFAULT
            )!!
                .setContentTitle("RX5")
                .setContentText("Connection service started")
                .setSmallIcon(R.drawable.ic_stat_connect)
                .setContentIntent(pendingIntent)
                .setTicker("Connection service started")
                .build()
        }

        startForeground(ONGOING_NOTIFICATION_ID, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String{
        val chan = NotificationChannel(channelId,
            channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.WHITE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    fun getNotificationBuilder(
        context: Context,
        channelId: String,
        importance: Int
    ): NotificationCompat.Builder? {
        val builder: NotificationCompat.Builder
        builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            prepareChannel(
                context,
                channelId,
                importance
            )
            NotificationCompat.Builder(context!!, channelId!!)
        } else {
            NotificationCompat.Builder(context)
        }
        return builder
    }

    @TargetApi(26)
    private fun prepareChannel(
        context: Context,
        id: String,
        importance: Int
    ) {
        val appName = context.getString(R.string.app_name)
        val description =
            CHANNEL_ID
        val nm =
            context.getSystemService(Activity.NOTIFICATION_SERVICE) as NotificationManager
        if (nm != null) {
            var nChannel = nm.getNotificationChannel(id)
            if (nChannel == null) {
                nChannel = NotificationChannel(id, appName, importance)
                nChannel.description = description
                nm.createNotificationChannel(nChannel)
            }
        }
    }

}

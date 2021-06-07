package com.koso.rx5.core

import android.annotation.TargetApi
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer


class Rx5ConnectionService : LifecycleService() {


    companion object {
        private val EXTRA_STOP = "extra_stop"
        private var NOTIFICATION_ID = 29

        fun startService(context: Context, macAddr: String, notifyId: Int) {
            NOTIFICATION_ID = notifyId
            val intent = Intent(context, Rx5ConnectionService::class.java)
            intent.putExtra("mac", macAddr)
            context.startService(intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, Rx5ConnectionService::class.java)
            intent.putExtra(EXTRA_STOP, true)
            context.startService(intent)
        }
    }


    /**
     * Channel id to build Notification
     */
    private val CHANNEL_ID = "RX5 Connection"

    private var macAddress: String = ""

    private val connectionStateObserver = Observer<Rx5Device.State> {
        when (it) {
            Rx5Device.State.Disconnected -> {
                stopSelf()
            }
            Rx5Device.State.Connected -> {
                postOngoingNotification()
            }
            Rx5Device.State.Connecting -> {
            }
            Rx5Device.State.Discovering -> {

            }
            else -> {

            }
        }
    }

    val binder = ConnectServiceBinder()

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class ConnectServiceBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        val serviceRx5: Rx5ConnectionService = this@Rx5ConnectionService
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent != null && intent.getBooleanExtra(EXTRA_STOP, false)) {
            stopSelf()
        } else {
            macAddress = intent?.getStringExtra("mac") ?: ""
            if (Rx5Handler.rx5 == null) {
                Rx5Handler.rx5 = Rx5Device(this, macAddress)
            }

            if (Rx5Handler.STATE_LIVE.value == Rx5Device.State.Disconnected) {
                Rx5Handler.rx5!!.connectAsClient(Rx5Handler.incomingCommandListener)
                registerConnectionState()
            }
        }

        return START_NOT_STICKY
    }

    private fun registerConnectionState() {
        Rx5Handler.STATE_LIVE.observe(this, connectionStateObserver)
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
            val cId = createNotificationChannel("rx5", CHANNEL_ID)
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
                NotificationManagerCompat.IMPORTANCE_LOW
            )!!
                .setContentTitle("RX5")
                .setContentText("Connection service started")
                .setSmallIcon(R.drawable.ic_stat_connect)
                .setContentIntent(pendingIntent)
                .setTicker("Connection service started")
                .build()
        }

        startForeground(NOTIFICATION_ID, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
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
            NotificationCompat.Builder(context, channelId)
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

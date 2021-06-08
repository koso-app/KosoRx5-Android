package com.koso.rx5.core

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.github.ivbaranov.rxbluetooth.BluetoothConnection
import com.github.ivbaranov.rxbluetooth.RxBluetooth
import com.koso.rx5.core.command.incoming.AvailableIncomingCommands
import com.koso.rx5.core.command.incoming.BaseIncomingCommand
import com.koso.rx5.core.command.outgoing.BaseOutgoingCommand
import com.koso.rx5.core.util.Utility
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*


open class Rx5Device(
    val context: Context,
    val mac: String,
    val SERVICE_UUID: String = "00001101-0000-1000-8000-00805F9B34FB"
) {

    enum class State {
        Disconnected, Discovering, Connected, Connecting
    }

    /**
     * The Bluetooth handler based on RxJava
     */
    private var rxBluetooth = RxBluetooth(context)


    private var btConnection: BluetoothConnection? = null

    /**
     * Collection of Rx disposable
     */
    private val compositeDisposable = CompositeDisposable()

    private val delimiters = 0x55


    /**
     *
     * Receiver for bluetooth disconnect
     */
    val bluetoothReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action != null && action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(
                    BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR
                )
                when (state) {
                    BluetoothAdapter.STATE_OFF -> {
                        Rx5Handler.setState(State.Disconnected)
                    }
                }
            }
            if (action != null && action == BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED) {

            }
            if (action != null && action == BluetoothDevice.ACTION_ACL_DISCONNECTED) {
                Rx5Handler.setState(State.Disconnected)
            }
        }
    }

    init {
        observeConnectionState()
        registerStateChange(context)
    }

    private fun unregisterDisconnect(context: Context) {
        context.unregisterReceiver(bluetoothReceiver)
    }

    private fun registerStateChange(context: Context) {

        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        val f1 = IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)
        val f2 = IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED)

        context.registerReceiver(bluetoothReceiver, filter)
        context.registerReceiver(bluetoothReceiver, f1)
        context.registerReceiver(bluetoothReceiver, f2)
    }

    /**
     * Observe the discover state, will be ACTION_DISCOVERY_STARTED, ACTION_DISCOVERY_FINISHED
     */
    private fun observeDiscoverState() {
        val dispo = rxBluetooth.observeDiscovery()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                when (it) {
                    BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                        Rx5Handler.setState(State.Discovering)
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {

                    }
                }
            }, { t -> t.printStackTrace() })

        compositeDisposable.add(dispo)

    }


    fun observeByteStream(): Flowable<Byte> {
        if (btConnection == null) throw NullPointerException("BluetoothConnection is not allowed to be null")
        return btConnection!!.observeByteStream()
    }

    fun observeStringStream(): Flowable<String> {
        if (btConnection == null) throw NullPointerException("BluetoothConnection is not allowed to be null")
        return btConnection!!.observeStringStream(delimiters)
    }

    /**
     * Start the remote device discovery process.
     */
    open fun startDiscovery() {

        rxBluetooth.startDiscovery()
    }

    /**
     * Cancel the current device discovery process.
     */
    open fun cancelDiscovery() {
        rxBluetooth.cancelDiscovery()
    }

    var buffer: MutableList<Byte> = mutableListOf()

    open fun connectAsClient(listener: IncomingCommandListener) {
        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        val uuid = UUID.fromString(SERVICE_UUID)
        val device = bluetoothAdapter?.getRemoteDevice(mac)
        val disposable = rxBluetooth.connectAsClient(device, uuid)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({ it ->
                btConnection = BluetoothConnection(it)
                btConnection?.observeByteStream()!!
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({ inByte: Byte ->
                        if ((buffer.size == 0 && inByte == 0xFF.toByte()) || buffer.size > 0) {
                            buffer.add(inByte)
                        }

                        when {
                            buffer.size == 2 -> {
                                val available = checkAvailableHead(buffer)
                                if (!available) {
                                    buffer.clear()
                                }
                            }
                            buffer.size > 4 -> {
                                val command = checkAvailableCommand(buffer)?.classObject?.newInstance()?.create(buffer)
                                if(command != null){
                                    listener.onCommandAvailable(command)
                                    buffer.clear()
                                }
                            }
                        }
                    }, {
                        it.printStackTrace()
                    })
                Rx5Handler.setState(State.Connected)
            },
                { t ->
                    Log.d("bt", "connectAsClient: ${t.localizedMessage}")
                    Rx5Handler.setState(State.Disconnected)
                })
        Rx5Handler.setState(State.Connecting)
        compositeDisposable.add(disposable)
    }

    private fun checkAvailableCommand(buffer: MutableList<Byte>): AvailableIncomingCommands? {
        AvailableIncomingCommands.values().forEach {
            if (buffer[0] == it.header1 && buffer[1] == it.header2 &&
                buffer[buffer.size - 2] == it.end1 && buffer[buffer.size - 1] == it.end2
            ) return it
        }
        return null
    }

    private fun checkAvailableHead(buffer: MutableList<Byte>): Boolean {
        AvailableIncomingCommands.values().forEach {
            if (buffer[0] == it.header1 && buffer[1] == it.header2) return true
        }
        return false
    }

    open fun connectAsServer() {
        val uuid = UUID.fromString(SERVICE_UUID)
        val disposable = rxBluetooth.connectAsServer("Rx5", uuid)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                btConnection = BluetoothConnection(it)
                btConnection?.observeByteStream()!!
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                    }, {
                        it.printStackTrace()
                    })
                Rx5Handler.setState(State.Connected)
            }, { t ->
                t.printStackTrace()
                Rx5Handler.setState(State.Disconnected)
            })

        Rx5Handler.setState(State.Connecting)
        compositeDisposable.add(disposable)
    }

    private fun observeConnectionState() {
        val dispo = rxBluetooth.observeConnectionState()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                when (it.state) {
                    BluetoothAdapter.STATE_DISCONNECTED -> {
                        Rx5Handler.setState(State.Disconnected)
                    }
                    BluetoothAdapter.STATE_CONNECTING -> {

                    }
                    BluetoothAdapter.STATE_DISCONNECTING -> {

                    }

                }
            }, { t ->
                t.printStackTrace()
            })
        compositeDisposable.add(dispo)
    }

    private fun observeAclEvent() {

        val dispo = rxBluetooth.observeAclEvent()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.computation())
            .subscribe({ aclEvent ->

                when (aclEvent.action) {
                    BluetoothDevice.ACTION_ACL_CONNECTED -> {

                    }
                    BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED -> {

                    }
                    BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                        Rx5Handler.setState(State.Disconnected)
                    }
                }
            }, { t -> t.printStackTrace() })

        compositeDisposable.add(dispo)
    }


    fun write(cmd: BaseOutgoingCommand): Boolean {
        Log.d("rx5", Utility.bytesToHex(cmd.encode()))
        Log.d("rx5", "------")
        Log.d("rx5", cmd.valueToString())
        Log.d("rx5", "------")

        return write(cmd.encode())
    }

    fun write(bytes: ByteArray): Boolean {
        if (btConnection == null) return false
        return btConnection?.send(bytes) ?: false
    }

    private fun disconnect() {
        btConnection?.closeConnection()
        btConnection = null
    }

    open fun destory() {
        unregisterDisconnect(context = context)
        disconnect()
        cancelDiscovery()
        compositeDisposable.clear()
        Rx5Handler.setState(State.Disconnected)
    }

    interface IncomingCommandListener{
        fun onCommandAvailable(cmd: BaseIncomingCommand)
    }
}
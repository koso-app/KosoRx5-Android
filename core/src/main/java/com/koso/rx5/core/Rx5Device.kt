package com.koso.rx5.core

import android.bluetooth.*
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
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


open class Rx5Device(
    val context: Context,
    val mac: String,
    val SERVICE_UUID: String = "00001101-0000-1000-8000-00805F9B34FB"
) {

//    val ServiceUuidString = "D88B7688-729D-BDA1-7A46-25F4104626C7"
    val ServiceUuidString = "92faec07-c075-4b7c-a6c2-bbd1d1a150f5" // Kawasaki
//    val ReadCharacteristicUuidString = "39D7AFB7-4ED7-4334-D79B-6675D916D7E3"
    val ReadCharacteristicUuidString = "3aabbb34-eac0-40f5-9d50-3a1ee6787136" // Kawasaki data source uuid
//    val WriteCharacteristicUuidString = "40E288F6-B367-F64A-A5F7-B4DFEE9F09E7"
    val WriteCharacteristicUuidString = "acf1b15c-10f9-4942-a32d-f9e019b95402" // Kawasaki control point uuid

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

    private var bluetoothGatt: BluetoothGatt? = null
    private var cmdListener: IncomingCommandListener? = null
    private var gattService: BluetoothGattService? = null
    private var gattWriteCharacteristic: BluetoothGattCharacteristic? = null
    private var gattReadCharacteristic: BluetoothGattCharacteristic? = null


    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            Log.d("bledebug", "onConnectionStateChange: $newState")
            bluetoothGatt = gatt
            when(newState){
                BluetoothProfile.STATE_CONNECTED -> {
                    GlobalScope.launch(Dispatchers.Main) {
                        Rx5Handler.setState(State.Connected)
                    }

                    GlobalScope.launch(Dispatchers.IO) {
                        delay(1000)
                        bluetoothGatt?.requestMtu(256)
                        delay(5000)
                        bluetoothGatt?.discoverServices()
                    }


                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    GlobalScope.launch(Dispatchers.Main) {
                        Rx5Handler.setState(State.Disconnected)
                        Rx5Handler.stopConnectService(context)
                    }
                    destory()
                }
                BluetoothProfile.STATE_CONNECTING -> {
                    GlobalScope.launch(Dispatchers.Main) {
                        Rx5Handler.setState(State.Connecting)
                    }
                }
                BluetoothProfile.STATE_DISCONNECTING -> {
                    GlobalScope.launch(Dispatchers.Main) {
                        Rx5Handler.setState(State.Connecting)
                    }
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if(status == BluetoothGatt.GATT_SUCCESS){
                gattService = gatt?.getService(UUID.fromString(ServiceUuidString))
                gattReadCharacteristic = gattService?.getCharacteristic(UUID.fromString(ReadCharacteristicUuidString))
                gattWriteCharacteristic = gattService?.getCharacteristic(UUID.fromString(WriteCharacteristicUuidString))

                gatt?.setCharacteristicNotification(gattReadCharacteristic, true)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            if(characteristic != null){
                handleInByte(characteristic.value)
            }

        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
        }

        private fun handleInByte(bytes: ByteArray) {
            bytes.forEach { inByte ->
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
                        val command =
                            checkAvailableCommand(buffer)?.classObject?.newInstance()
                                ?.create(buffer)
                        if (command != null) {
                            cmdListener?.onCommandAvailable(command)
                            buffer.clear()
                        }
                    }
                }
            }

        }
    }



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
        RxJavaPlugins.setErrorHandler { e -> Log.d("rx5device", e.message ?: "") }
    }

    private fun unregisterDisconnect(context: Context) {
        try {
            context.unregisterReceiver(bluetoothReceiver)
        }catch (e: IllegalArgumentException){
        }
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
                val dispo = btConnection?.observeByteStream()!!
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
                                val command =
                                    checkAvailableCommand(buffer)?.classObject?.newInstance()
                                        ?.create(buffer)
                                if (command != null) {
                                    listener.onCommandAvailable(command)
                                    buffer.clear()
                                }
                            }
                        }
                    }, {
                        it.printStackTrace()
                    })
                Rx5Handler.setState(State.Connected)
                compositeDisposable.add(dispo)
            },
                { t ->
                    Log.d("bt", "connectAsClient: ${t.localizedMessage}")
                    Rx5Handler.setState(State.Disconnected)
                })
        Rx5Handler.setState(State.Connecting)
        compositeDisposable.add(disposable)
    }

    /**
     * Start connect process for BLE connection
     */
    fun connectToGattServer(listener: IncomingCommandListener): Boolean {
        cmdListener = listener

        val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
        bluetoothAdapter?.let { adapter ->
            try {
                val device = adapter.getRemoteDevice(mac)
                bluetoothGatt = device.connectGatt(context, false, bluetoothGattCallback)

            } catch (exception: IllegalArgumentException) {
                Log.w("bledebug", "Device not found with provided address.")
                destory()
                Rx5Handler.setState(State.Disconnected)
                return false
            }
            // connect to the GATT server on the device
        } ?: run {
            Log.w("bledebug", "BluetoothAdapter not initialized")
            Rx5Handler.setState(State.Disconnected)
            return false
        }
        return true
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

    fun writeLe(cmd: BaseOutgoingCommand): Boolean{
        val success =  gattWriteCharacteristic?.setValue(cmd.encode()) ?: false
        if(success) {
            return bluetoothGatt?.writeCharacteristic(gattWriteCharacteristic) ?: false
        }
        return false
    }

    fun write(cmd: BaseOutgoingCommand): Boolean {
//        Log.d("rx5", Utility.bytesToHex(cmd.encode()))
//        Log.d("rx5", "------")
//        Log.d("rx5", cmd.valueToString())
//        Log.d("rx5", "------")

        return write(cmd.encode())
    }

    fun write(bytes: ByteArray): Boolean {
        if (btConnection == null) return false
        return btConnection?.send(bytes) ?: false
    }

    private fun disconnect() {
        try {
            btConnection?.closeConnection()
            btConnection = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    open fun destory() {

        //for ble
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null

        gattService = null
        gattWriteCharacteristic = null
        gattReadCharacteristic = null

        // for classic
        compositeDisposable.dispose()
        unregisterDisconnect(context = context)
        cancelDiscovery()
        disconnect()




    }

    interface IncomingCommandListener {
        fun onCommandAvailable(cmd: BaseIncomingCommand)
    }
}
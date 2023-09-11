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
import com.koso.rx5.core.command.incoming.KawasakiCommand
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
    val SERVICE_UUID: String = "92faec07-c075-4b7c-a6c2-bbd1d1a150f5" // for bluetooth classic, skip for BLE
) {

    //    val ServiceUuidString = "D88B7688-729D-BDA1-7A46-25F4104626C7"
//    val ReadCharacteristicUuidString = "39D7AFB7-4ED7-4334-D79B-6675D916D7E3"
//    val WriteCharacteristicUuidString = "40E288F6-B367-F64A-A5F7-B4DFEE9F09E7"
    val DATASOURCE_UUID = "3aabbb34-eac0-40f5-9d50-3a1ee6787136"
    val DATASOURCE_MID_UUID = "5e119eba-35a7-4463-a7af-7fa40a302350"
    val DATASOURCE_HIGH_UUID = "02fad1bd-358e-441c-b296-fe874af38a7e"
    val CONTROLPOINT_UUID = "acf1b15c-10f9-4942-a32d-f9e019b95402"
    val CONFIG_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb"

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
    private var dataCharacteristic: BluetoothGattCharacteristic? = null
    private var dataMidCharacteristic: BluetoothGattCharacteristic? = null
    private var dataHighCharacteristic: BluetoothGattCharacteristic? = null


    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            bluetoothGatt = gatt
            when(newState){
                BluetoothProfile.STATE_CONNECTED -> {
                    GlobalScope.launch(Dispatchers.Main) {
                        Rx5Handler.setState(State.Connected)
                    }

                    GlobalScope.launch(Dispatchers.IO) {
                        delay(1000)
                        gatt?.requestMtu(242)
                        gatt?.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
                        delay(5000)
                        bluetoothGatt?.discoverServices()
                    }


                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    GlobalScope.launch(Dispatchers.Main) {
                        Rx5Handler.setState(State.Disconnected)
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
                gattService = gatt?.getService(UUID.fromString(SERVICE_UUID))
                dataCharacteristic = gattService?.getCharacteristic(UUID.fromString(DATASOURCE_UUID))
                dataMidCharacteristic = gattService?.getCharacteristic(UUID.fromString(DATASOURCE_MID_UUID))
                dataHighCharacteristic = gattService?.getCharacteristic(UUID.fromString(DATASOURCE_HIGH_UUID))
                gattWriteCharacteristic = gattService?.getCharacteristic(UUID.fromString(CONTROLPOINT_UUID))
                if(dataCharacteristic != null) {

                    GlobalScope.launch {
                        if(dataCharacteristic != null) {

                            val desc: BluetoothGattDescriptor =
                                dataCharacteristic!!.getDescriptor(UUID.fromString(CONFIG_DESCRIPTOR))
                            desc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            var success = gatt!!.writeDescriptor(desc)
                            delay(1000)
                            gatt.setCharacteristicNotification(dataCharacteristic, true)
                        }

                        if(dataMidCharacteristic != null) {
                            delay(1000)
                            val desc1: BluetoothGattDescriptor =
                                dataMidCharacteristic!!.getDescriptor(UUID.fromString(CONFIG_DESCRIPTOR))
                            desc1.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            var success1 = gatt!!.writeDescriptor(desc1)
                            delay(1000)
                            gatt.setCharacteristicNotification(dataMidCharacteristic, true)
                        }

                        if(dataHighCharacteristic != null) {
                            delay(1000)
                            val desc2: BluetoothGattDescriptor =
                                dataHighCharacteristic!!.getDescriptor(UUID.fromString(CONFIG_DESCRIPTOR))
                            desc2.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            var success2 = gatt!!.writeDescriptor(desc2)
                            delay(1000)
                            gatt.setCharacteristicNotification(dataHighCharacteristic, true)
                        }
                    }
                }
            }
        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorRead(gatt, descriptor, status)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            if(characteristic != null){

                handleInByte(characteristic)
                characteristic.setValue(byteArrayOf())
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

        private fun handleInByte(characteristic: BluetoothGattCharacteristic) {
            val command = KawasakiCommand().apply {
                this.parseData(characteristic.value)
                this.title = if(characteristic.uuid.toString().equals(DATASOURCE_HIGH_UUID, true)){
                    "DATA SOURCE HIGH"
                } else if(characteristic.uuid.toString().equals(DATASOURCE_MID_UUID, true)) {
                    "DATA SOURCE MID"
                } else{
                    "DATA SOURCE LOW"
                }
            }
            cmdListener?.onCommandAvailable(command)
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

    // for bluetooth classic
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
                destory()
                Rx5Handler.setState(State.Disconnected)
                return false
            }
            // connect to the GATT server on the device
        } ?: run {
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

    fun writeLe(bytes: ByteArray): Boolean{
        val success =  gattWriteCharacteristic?.setValue(bytes) ?: false
        if(success) {
            return bluetoothGatt?.writeCharacteristic(gattWriteCharacteristic) ?: false
        }
        return false
    }

    fun writeLe(cmd: BaseOutgoingCommand): Boolean{
        return writeLe(cmd.encode())
    }

    fun write(cmd: BaseOutgoingCommand): Boolean {
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
        dataCharacteristic = null
        dataMidCharacteristic = null
        dataHighCharacteristic = null

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
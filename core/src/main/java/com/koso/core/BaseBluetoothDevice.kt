package com.koso.core

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.ivbaranov.rxbluetooth.BluetoothConnection
import com.github.ivbaranov.rxbluetooth.RxBluetooth
import com.koso.core.command.BaseCommand
import com.koso.core.command.NaviInfoCommand
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.util.*

abstract class BaseBluetoothDevice(context: Context, val SERVICE_UUID: String) {

    enum class State {
        Disconnected, Discovering, Connected, Connecting
    }


    /**
     * The Bluetooth handler based on RxJava
     */
    private var rxBluetooth = RxBluetooth(context)


    private var btConnection: BluetoothConnection? = null


    /**
     * Inner LiveData for updating the state observable value
     */
    private val _stateLive: MutableLiveData<State> = MutableLiveData<State>().apply{
        this.value = State.Disconnected
    }

    /**
     * External LiveData for accessing the latest state
     */
    val stateLive: LiveData<State> = _stateLive

    /**
     * Collection of Rx disposable
     */
    private val compositeDisposable = CompositeDisposable()

    init {
        observeAclEvent()
        observeDiscoverState()
    }

    private fun observeDiscoverState() {
        val dispo = rxBluetooth.observeDiscovery()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                when (it) {
                    BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                        _stateLive.value = State.Discovering
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        _stateLive.value = State.Disconnected
                    }
                }
            }, {t -> t.printStackTrace()})

        compositeDisposable.add(dispo)

    }


    /**
     * Return true if Bluetooth is available.
     *
     * @return true if bluetoothAdapter is not null or it's address is empty, otherwise Bluetooth is
     * not supported on this hardware platform
     */
    fun isBluetoothAvailable(): Boolean {
        return rxBluetooth.isBluetoothAvailable
    }

    /**
     * Return true if Bluetooth is currently enabled and ready for use.
     *
     * Requires [android.Manifest.permission.BLUETOOTH]
     *
     * @return true if the local adapter is turned on
     */
    fun isBluetoothEnabled(): Boolean {
        return rxBluetooth.isBluetoothEnabled
    }

    /**
     * Return true if a location service is enabled.
     *
     * @return true if either the GPS or Network provider is enabled
     */
    fun isLocationServiceEnabled(): Boolean {
        return rxBluetooth.isLocationServiceEnabled
    }

    /**
     * This will issue a request to enable Bluetooth through the system settings (without stopping
     * your application) via ACTION_REQUEST_ENABLE action Intent.
     *
     * @param activity Activity
     * @param requestCode request code
     */
    fun enableBluetooth(activity: Activity, requestCode: Int) {
        rxBluetooth.enableBluetooth(activity, requestCode)
    }

    /**
     * Observes Bluetooth devices found while discovering.
     *
     * @return RxJava Observable with BluetoothDevice found
     */
    open fun observeDevices(): Observable<BluetoothDevice> {
        return rxBluetooth.observeDevices()
    }

    fun observeByteStream(): Flowable<Byte> {
        if (btConnection == null) throw NullPointerException("BluetoothConnection is not allowed to be null")
        return btConnection!!.observeByteStream()
    }

    fun observeStringStream(): Flowable<String> {
        if (btConnection == null) throw NullPointerException("BluetoothConnection is not allowed to be null")
        return btConnection!!.observeStringStream()
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

    /**
     * Create connection to [BluetoothDevice] and returns a connected [BluetoothSocket]
     * on successful connection. Notifies observers with [IOException] via `onError()`.
     *
     * @param bluetoothDevice bluetooth device to connect
     * @param uuid uuid for SDP record
     * @return Single with connected [BluetoothSocket] on successful connection
     */
    open fun connectAsClient(
        bluetoothDevice: BluetoothDevice
    ) {
        val uuid = UUID.fromString(SERVICE_UUID)

        val disposable = rxBluetooth.connectAsClient(bluetoothDevice, uuid)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                btConnection = BluetoothConnection(it)
                btConnection?.observeByteStream()!!
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                        Log.d("rx5debug", it.toString())
                    }, {
                        it.printStackTrace()
                    })
                _stateLive.value = State.Connected
            }, { t ->
                t.printStackTrace()
                _stateLive.value = State.Disconnected
            })

        _stateLive.value = State.Connecting
        compositeDisposable.add(disposable)
    }

    private fun observeAclEvent() {
        val dispo = rxBluetooth.observeAclEvent()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.computation())
            .subscribe ({ aclEvent ->

                when (aclEvent.action) {
                    BluetoothDevice.ACTION_ACL_CONNECTED -> {

                    }
                    BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED -> {

                    }
                    BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                        _stateLive.value = State.Disconnected
                    }
                }
            }, {t -> t.printStackTrace()})

        compositeDisposable.add(dispo)
    }


    fun write(cmd: BaseCommand){
        write(cmd.encode())
    }

    fun write(bytes: ByteArray) {
        btConnection?.send(bytes)
    }

    fun disconnect(){
        btConnection?.closeConnection()

    }

    open fun destory() {
        disconnect()
        cancelDiscovery()
        compositeDisposable.clear()
    }
}
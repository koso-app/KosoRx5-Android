package com.koso.core

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.ivbaranov.rxbluetooth.BluetoothConnection
import com.github.ivbaranov.rxbluetooth.RxBluetooth
import com.koso.core.command.BaseCommand
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*

class BaseBluetoothDevice(
    context: Context,
    val SERVICE_UUID: String = "00001101-0000-1000-8000-00805F9B34FB"
) {

    enum class State {
        Disconnected, Discovering, Connected, Connecting
    }

    companion object {
        /**
         * Inner LiveData for updating the state observable value
         */
        private val _stateLive: MutableLiveData<State> = MutableLiveData<State>().apply {
            this.value = State.Disconnected
        }

        /**
         * External LiveData for accessing the latest state
         */
        val stateLive: LiveData<State> = _stateLive
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

    init {
        observeAclEvent()
        observeConnectionState()
        observeDiscoverState()
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


    open fun connectAsServer(){
        val uuid = UUID.fromString(SERVICE_UUID)
        val disposable = rxBluetooth.connectAsServer("Rx5", uuid )
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
            },{ t ->
                t.printStackTrace()
                _stateLive.value = State.Disconnected
            })

        _stateLive.value = State.Connecting
        compositeDisposable.add(disposable)
    }

    private fun observeConnectionState() {
        val dispo = rxBluetooth.observeConnectionState()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                when (it.state) {
                    BluetoothAdapter.STATE_DISCONNECTED -> {
                        _stateLive.value = State.Disconnected
                    }
                    BluetoothAdapter.STATE_CONNECTING -> {
                        _stateLive.value = State.Connecting
                    }
                    BluetoothAdapter.STATE_CONNECTED -> {
                        _stateLive.value = State.Connected
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


    fun write(cmd: BaseCommand): Boolean{
        return write(cmd.encode())
    }

    fun write(bytes: ByteArray): Boolean {
        if (btConnection == null) return false
        return btConnection?.send(bytes) ?: false
    }

    fun disconnect(){
        btConnection?.closeConnection()
        _stateLive.value = State.Disconnected
    }

    open fun destory() {
        disconnect()
        cancelDiscovery()
        compositeDisposable.clear()
    }
}
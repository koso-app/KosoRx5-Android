package com.koso.rx5.core

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.ivbaranov.rxbluetooth.RxBluetooth
import com.koso.rx5.core.Rx5Handler.startDiscovery
import io.reactivex.Flowable
import io.reactivex.Observable

object Rx5Handler{


    /**
     * Inner LiveData for updating the state observable value
     */
    private val _stateLive: MutableLiveData<BaseBluetoothDevice.State> =
        MutableLiveData<BaseBluetoothDevice.State>().apply {
            this.value = BaseBluetoothDevice.State.Disconnected
        }

    /**
     * External LiveData for accessing the latest state
     */
    val stateLive: LiveData<BaseBluetoothDevice.State> = _stateLive


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

    /**
     * Observes Bluetooth devices found while discovering.
     *
     * @return RxJava Observable with BluetoothDevice found
     */
    open fun observeDevices(rxBluetooth: RxBluetooth): Observable<BluetoothDevice> {
        return rxBluetooth.observeDevices()
    }


    /**
     * Start the remote device discovery process.
     */
    open fun startDiscovery(rxBluetooth: RxBluetooth) {

        rxBluetooth.startDiscovery()
    }



    fun destory() {
        rx5?.destory()
        rx5 = null
        setState(BaseBluetoothDevice.State.Disconnected)
    }

    fun setState(s: BaseBluetoothDevice.State) {
        if (_stateLive.value != s) _stateLive.value = s
    }

}
package com.koso.rx5.core

import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.ivbaranov.rxbluetooth.RxBluetooth
import com.koso.rx5.core.command.incoming.BaseIncomingCommand
import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object Rx5Handler{


    /**
     * Inner LiveData for updating the state observable value
     */
    private val __STATE_LIVE: MutableLiveData<Rx5Device.State> =
        MutableLiveData<Rx5Device.State>().apply {
            this.value = Rx5Device.State.Disconnected
        }

    /**
     * External LiveData for accessing the latest state
     */
    val STATE_LIVE: LiveData<Rx5Device.State> = __STATE_LIVE


    var rx5: Rx5Device? = null

    /**
     * Listener of the incoming commands
     */
    val incomingCommandListener = object: Rx5Device.IncomingCommandListener{
        override fun onCommandAvailable(cmd: BaseIncomingCommand) {
            _incomingCommandLive.postValue(cmd)
        }
    }
    private val _incomingCommandLive = MutableLiveData<BaseIncomingCommand>()
    fun incomingCommandLive(): LiveData<BaseIncomingCommand> = _incomingCommandLive


    /**
     * Start connection service
     */
    fun startConnectService(context: Context, address: String){
        Rx5ConnectionService.startService(context, address)
    }

    // to move bluetooth connect to ble
    fun startLeConnectService(context: Context, address: String){

        Rx5BleConnectionService.startService(context, address)
    }
    /**
     * Stop connection service
     */
    fun stopConnectService(context: Context){
        Rx5ConnectionService.stopService(context)
    }

    fun stopLeConnectService(context: Context){
        Rx5BleConnectionService.stopService(context)
    }

    /**
     * Observes Bluetooth devices found while discovering.
     *
     * @return RxJava Observable with BluetoothDevice found
     */
    fun observeDevices(rxBluetooth: RxBluetooth): Observable<BluetoothDevice> {
        return rxBluetooth.observeDevices()
    }

    /**
     * Start the remote device discovery process.
     */
    fun startDiscovery(rxBluetooth: RxBluetooth) {

        rxBluetooth.startDiscovery()
    }

    fun destory() {
        rx5?.destory()
        rx5 = null
        setState(Rx5Device.State.Disconnected)
    }

    fun setState(s: Rx5Device.State) {
        if (__STATE_LIVE.value != s) __STATE_LIVE.value = s
    }

}
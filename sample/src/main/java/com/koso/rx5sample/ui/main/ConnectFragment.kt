package com.koso.rx5sample.ui.main

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.koso.core.BaseBluetoothDevice
import com.koso.core.Rx5
import com.koso.rx5sample.App
import com.koso.rx5sample.R
import com.koso.rx5sample.service.ConnectionService
import com.koso.rx5sample.utils.SharedPreferenceHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_connect.*

/**
 * A placeholder fragment containing a simple view.
 */
class ConnectFragment : Fragment() {

    companion object{
        fun newInstance(): Fragment {
            return ConnectFragment()
        }

    }


    /**
     * Request turn on the BT when it's not available
     */
    private val REQUEST_ENABLE_BT: Int = 10

    /**
     * MVVM viewmodel
     */
    private lateinit var viewModel: TabbedViewModel

    /**
     *
     */
    private var rx5: Rx5? = null


    private var service: ConnectionService? = null

    /**
     * Handles all observable disposables
     */
    private val compositeDisposable = CompositeDisposable()

    /**
     * The connection state call back for ConnectionService
     */
    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            rx5 = null
            service = null
        }

        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            service = (binder as ConnectionService.ConnectServiceBinder).service
            service?.let {
                rx5 = it.rx5


                defaultConnect()
                subscribeStateEvent()
                subscribeDevices()
            }
        }
    }

    private fun defaultConnect() {

        val device = BluetoothAdapter.getDefaultAdapter()
            .getRemoteDevice(SharedPreferenceHandler.targetMacAddress)
        rx5?.cancelDiscovery()
        rx5?.connectAsClient(device)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!).get(TabbedViewModel::class.java)

        val intent = Intent(App.instance, ConnectionService::class.java)
        App.instance.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_connect, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    override fun onResume() {
        super.onResume()
        rx5?.let {
            updateStateUi(it.stateLive.value)
        }
    }

    private fun initViews() {
        vStart.setOnClickListener {

            rx5?.let {
                if (!it.isBluetoothAvailable()) {
                    // handle the lack of bluetooth support
                } else {
                    // check if bluetooth is currently enabled and ready for use
                    if (!it.isBluetoothEnabled()) {
                        // to enable bluetooth via startActivityForResult()
                        it.enableBluetooth(activity as Activity, REQUEST_ENABLE_BT);
                    } else {

                        when(it.stateLive.value){
                            BaseBluetoothDevice.State.Connected -> {
                                it.destory()
                            }
                            BaseBluetoothDevice.State.Discovering -> {
                                it.cancelDiscovery()
                            }
                            BaseBluetoothDevice.State.Connecting -> {
                                it.destory()
                            }
                            else -> {
//                                it.startDiscovery()
                                defaultConnect()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun subscribeStateEvent() {
        rx5?.stateLive?.observe(this, Observer{
            viewModel.log(it.name)
            updateStateUi(it)
        })
    }

    private fun updateStateUi(it: BaseBluetoothDevice.State?) {

        when (it) {
            BaseBluetoothDevice.State.Disconnected -> {
                vStart.setText(R.string.disconnected)
                vStart.setBackgroundResource(R.drawable.ripple_oval_btn_disconnect)
            }
            BaseBluetoothDevice.State.Connected -> {
                vStart.setText(R.string.connected)
                vStart.setBackgroundResource(R.drawable.ripple_oval_btn_connect)
            }
            BaseBluetoothDevice.State.Discovering -> {
                vStart.setText(R.string.discovering)
                vStart.setBackgroundResource(R.drawable.ripple_oval_btn_progress)
            }
            BaseBluetoothDevice.State.Connecting -> {
                vStart.setText(R.string.connecting)
                vStart.setBackgroundResource(R.drawable.ripple_oval_btn_progress)
            }
            else -> {

            }
        }
    }


    private fun subscribeDevices() {
        rx5?.let {
            val disposable = it.observeDevices()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .subscribe({d ->
                    d?.let {device ->

                        Log.d("rx5debug", "${device.name} ${device.address} found")

                        if (device.name == "Koso-BT") {
                            viewModel.log("Koso-BT found")
                            it.cancelDiscovery()
                            it.connectAsClient(device)
                            vStart.text = "${device.name} found"
                        }
                    }
                }, {t ->
                    t.printStackTrace()
                })

            compositeDisposable.add(disposable)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

}
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
import com.koso.core.ConnectionService
import com.koso.core.Rx5
import com.koso.core.util.Utility
import com.koso.rx5sample.App
import com.koso.rx5sample.R
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


    private var service: ConnectionService? = null

    /**
     * Handles all observable disposables
     */
    private val compositeDisposable = CompositeDisposable()

    /**
     * The buffer of the incoming byte from Bluetooth
     */
    private var incomingByte = byteArrayOf()


    private fun subscribeByteStream() {
        val dispo = Rx5.instance?.observeStringStream()
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribeOn(Schedulers.io())
            ?.subscribe({
                viewModel.log("received byte: ${Utility.bytesToHex(it.toByteArray())}")
            }, {})

        dispo?.let{
            compositeDisposable.add(it)
        }
    }

    private fun connectAsServer(){
        activity?.startService(Intent(activity, ConnectionService::class.java))
        Rx5.connect(activity!!)
    }

    private fun connectAsClient() {

        val device = BluetoothAdapter.getDefaultAdapter()
            .getRemoteDevice(SharedPreferenceHandler.targetMacAddress)
        Rx5.instance?.cancelDiscovery()
        Rx5.instance?.connectAsClient(device)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!).get(TabbedViewModel::class.java)
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
        Rx5.instance?.let {
            updateStateUi(it.stateLive.value)
        }
    }

    private fun initViews() {
        vStart.setOnClickListener {

            Rx5.instance?.let {
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
                                Rx5.disconnect(activity!!)
                                service = null
                            }
                            BaseBluetoothDevice.State.Discovering -> {
                                it.cancelDiscovery()
                            }
                            BaseBluetoothDevice.State.Connecting -> {
                                it.destory()
                            }
                            else -> {
//                                it.startDiscovery()
                                connectAsServer()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun subscribeStateEvent() {
        Rx5.instance?.stateLive?.observe(this, Observer{
            viewModel.log(it.name)
            updateStateUi(it)
            if(it == BaseBluetoothDevice.State.Connected){
                subscribeByteStream()
            }
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
        Rx5.instance?.let {
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
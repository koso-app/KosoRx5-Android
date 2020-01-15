package com.koso.rx5sample.ui.main

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.koso.core.BaseBluetoothDevice
import com.koso.core.ConnectionService
import com.koso.core.Rx5Handler
import com.koso.core.util.Utility
import com.koso.rx5sample.R
import com.koso.rx5sample.utils.BluetoothUtil
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
     * Handles all observable disposables
     */
    private val compositeDisposable = CompositeDisposable()

    /**
     * The buffer of the incoming byte from Bluetooth
     */
    private var incomingByte = byteArrayOf()


    private fun subscribeByteStream() {
        val dispo = Rx5Handler.rx5?.observeStringStream()
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribeOn(Schedulers.io())
            ?.subscribe({
                viewModel.log("received byte: ${Utility.bytesToHex(it.toByteArray())}")
            }, {})

        dispo?.let{
            compositeDisposable.add(it)
        }
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
        subscribeStateEvent()
        updateStateUi(BaseBluetoothDevice.stateLive.value)
    }

    private fun initViews() {
        vStart.setOnClickListener {

            if (!BluetoothUtil.checkAndRequestBluetooth(
                    activity as AppCompatActivity,
                    REQUEST_ENABLE_BT
                )
            ) {
                    // handle the lack of bluetooth support
                } else {


                when (BaseBluetoothDevice.stateLive.value) {
                    BaseBluetoothDevice.State.Connected -> {
                        Rx5Handler.stopConnectService(activity!!)
                    }
                    BaseBluetoothDevice.State.Discovering -> {
                        Rx5Handler.rx5?.cancelDiscovery()
                    }
                    BaseBluetoothDevice.State.Connecting -> {
                        Rx5Handler.rx5?.destory()
                    }
                    else -> {
                        Rx5Handler.startConnectService(activity as Context)
                        }
                    }
            }
        }
    }

    private fun subscribeStateEvent() {
        BaseBluetoothDevice.stateLive.observe(this, Observer {
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

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

}
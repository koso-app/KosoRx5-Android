package com.koso.rx5sample.ui.main

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.koso.rx5.core.BaseBluetoothDevice
import com.koso.rx5.core.Rx5Handler
import com.koso.rx5.core.util.Utility
import com.koso.rx5sample.R
import com.koso.rx5sample.utils.BluetoothUtil
import com.koso.rx5sample.utils.SharedPreferenceHandler
import com.koso.rx5sample.widgets.BlScanDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_connect.*

/**
 * A placeholder fragment containing a simple view.
 */
class ConnectFragment : Fragment() {

    companion object {
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

    private var byteBuffer = arrayListOf<Byte>()


    private fun subscribeByteStream() {

//        val dispo = Rx5Handler.rx5?.observeStringStream()
//            ?.observeOn(AndroidSchedulers.mainThread())
//            ?.subscribeOn(Schedulers.io())
//            ?.subscribe({
//                viewModel.log("received string: ${Utility.bytesToHex(it.toByteArray())}")
//            }, {})

        val dispo1 = Rx5Handler.rx5?.observeByteStream()
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribeOn(Schedulers.io())
            ?.subscribe({
                viewModel.log("received byte: ${Utility.bytesToHex(byteArrayOf(it))}")
            }, {})

//        dispo?.let{
//            compositeDisposable.add(it)
//        }

        dispo1?.let {
            compositeDisposable.add(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(requireActivity()).get(TabbedViewModel::class.java)
        subscribeStateEvent()
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

        updateStateUi(Rx5Handler.stateLive.value)
    }

    private fun initViews() {
        vScan.setOnClickListener{
            BlScanDialog().show(childFragmentManager, null)
        }
        vStart.setOnClickListener {
            val mac = SharedPreferenceHandler.targetMacAddress
            if (!BluetoothUtil.checkAndRequestBluetooth(
                    activity as AppCompatActivity,
                    REQUEST_ENABLE_BT
                )
            ) {
                // handle the lack of bluetooth support
            } else if (mac.isEmpty()) {
                BlScanDialog().show(childFragmentManager, null)
                return@setOnClickListener
            }else{


                when (Rx5Handler.stateLive.value) {
                    BaseBluetoothDevice.State.Connected -> {
                        Rx5Handler.stopConnectService(requireActivity())
                    }
                    BaseBluetoothDevice.State.Connecting -> {
                        Rx5Handler.stopConnectService(requireActivity())
                    }
                    else -> {
                        Rx5Handler.startConnectService(activity as Context, SharedPreferenceHandler.targetMacAddress)
                    }
                }
            }
        }
    }

    private fun subscribeStateEvent() {
        Rx5Handler.stateLive.observe(this, Observer {
//            viewModel.log(it.name)
            updateStateUi(it)
            if (it == BaseBluetoothDevice.State.Connected) {
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
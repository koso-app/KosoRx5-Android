package com.koso.rx5sample.ui.main

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.koso.rx5.core.Rx5Device
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


    private val blSelectListener = object: BlScanDialog.BlSelectListener{
        override fun onSelect() {
            vStart.performClick()
        }
    }
    private val sharedPrefListener = object: SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onSharedPreferenceChanged(
            sharedPreferences: SharedPreferences?,
            key: String?
        ) {
            if (key == SharedPreferenceHandler.PARAM_TARGET_MAC_ADDR) {
                showDeviceAddress(SharedPreferenceHandler.targetMacAddress)
            }
        }
    }

    private fun subscribeByteStream() {

        val dispo1 = Rx5Handler.rx5?.observeByteStream()
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribeOn(Schedulers.io())
            ?.subscribe({
                viewModel.log("received byte: ${String.format("%02X", it)}")
            }, {})

        dispo1?.let {
            compositeDisposable.add(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(requireActivity()).get(TabbedViewModel::class.java)
        subscribeStateEvent()
        setHasOptionsMenu(true)
        SharedPreferenceHandler.getSharedPrefences().registerOnSharedPreferenceChangeListener(sharedPrefListener)
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

        updateStateUi(Rx5Handler.STATE_LIVE.value)
        val mac = SharedPreferenceHandler.targetMacAddress
        showDeviceAddress(mac)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        SharedPreferenceHandler.getSharedPrefences().unregisterOnSharedPreferenceChangeListener(sharedPrefListener)
    }
    private fun showDeviceAddress(mac: String?) {
        if (mac != null) {
            device.text = if (mac.isEmpty()) getString(R.string.no_device) else String.format(
                getString(R.string.connect_to),
                mac
            )
        }else{
            device.text = getString(R.string.no_device)
        }

    }


    private fun initViews() {

        vScan.setOnClickListener {
            when (Rx5Handler.STATE_LIVE.value) {
                Rx5Device.State.Connected -> {
                    Rx5Handler.stopConnectService(requireActivity())
                    BlScanDialog().setBlSelectListener(blSelectListener).show(childFragmentManager, null)
                }
                Rx5Device.State.Connecting -> {
                    Rx5Handler.stopConnectService(requireActivity())
                    BlScanDialog().setBlSelectListener(blSelectListener).show(childFragmentManager, null)
                }
                else -> {
                    BlScanDialog().setBlSelectListener(blSelectListener).show(childFragmentManager, null)
                }
            }

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
                BlScanDialog().setBlSelectListener(blSelectListener).show(childFragmentManager, null)
                return@setOnClickListener
            } else {

                when (Rx5Handler.STATE_LIVE.value) {
                    Rx5Device.State.Connected -> {
                        Rx5Handler.stopConnectService(requireActivity())
                    }
                    Rx5Device.State.Connecting -> {
                        Rx5Handler.stopConnectService(requireActivity())
                    }
                    else -> {
                        Rx5Handler.startConnectService(
                            activity as Context,
                            SharedPreferenceHandler.targetMacAddress,
                            100
                        )
                    }
                }
            }
        }

        if (SharedPreferenceHandler.targetMacAddress.isNotEmpty()) {
            vStart.performClick()
        }
    }

    private fun subscribeStateEvent() {
        Rx5Handler.STATE_LIVE.observe(this, Observer {
//            viewModel.log(it.name)
            updateStateUi(it)
            if (it == Rx5Device.State.Connected) {
                subscribeByteStream()
            }
        })
    }

    private fun updateStateUi(it: Rx5Device.State?) {

        when (it) {
            Rx5Device.State.Disconnected -> {
                vStart.setText(R.string.disconnected)
                vStart.setBackgroundResource(R.drawable.ripple_oval_btn_disconnect)
            }
            Rx5Device.State.Connected -> {
                vStart.setText(R.string.connected)
                vStart.setBackgroundResource(R.drawable.ripple_oval_btn_connect)

            }
            Rx5Device.State.Discovering -> {
                vStart.setText(R.string.discovering)
                vStart.setBackgroundResource(R.drawable.ripple_oval_btn_progress)
            }
            Rx5Device.State.Connecting -> {
                vStart.setText(R.string.connecting)
                vStart.setBackgroundResource(R.drawable.ripple_oval_btn_progress)
            }
            else -> {

            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_tabbed_activity, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.search) {
            vScan.performClick()
            return true
        } else {
            return false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

}
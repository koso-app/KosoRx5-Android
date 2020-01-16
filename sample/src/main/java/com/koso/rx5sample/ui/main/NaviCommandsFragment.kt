package com.koso.rx5sample.ui.main

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.koso.core.BaseBluetoothDevice
import com.koso.core.ConnectionService
import com.koso.core.Rx5Handler
import com.koso.core.command.NaviInfoCommand
import com.koso.rx5sample.App
import com.koso.rx5sample.R
import kotlinx.android.synthetic.main.fragment_navicommands.*

/**
 * A placeholder fragment containing a simple view.
 */
class NaviCommandsFragment : Fragment() {


    private lateinit var viewmodel: TabbedViewModel


    private var service: ConnectionService? = null

    /**
     * The connection state call back for ConnectionService
     */
    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
        }

        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            service = (binder as ConnectionService.ConnectServiceBinder).service
            service?.let {
                subscribeStateEvent()
            }
        }
    }

    private fun subscribeStateEvent() {
        Rx5Handler.stateLive.observe(this, Observer {
            when (it) {
                BaseBluetoothDevice.State.Disconnected -> {

                }
                BaseBluetoothDevice.State.Connected -> {

                }
                BaseBluetoothDevice.State.Discovering -> {

                }
                BaseBluetoothDevice.State.Connecting -> {

                }
                else -> {

                }
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewmodel = ViewModelProviders.of(activity!!).get(TabbedViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_navicommands, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {

        vSend.setOnClickListener {

            val mode: Int = navimode.selectedItem.toString().toInt()
            val cityname = ctname.text.toString()
            val roadname = nowroadname.text.toString()
            val doornum = doornum.text.toString()
            val limitsp = limitsp.text.toString().toInt()
            val nextroad = nextroad.text.toString()
            val nextdist = nextdist.text.toString().toInt()
            val nextturn = turntype.text.toString().toInt()
            val camera = camera.text.toString().toInt()
            val navidist = navidist.text.toString().toInt()
            val navitime = navitime.text.toString().toInt()
            val gpsnum = gpsnum.text.toString().toInt()
            val gpsdir = gpsdir.text.toString().toInt()

            val cmd = NaviInfoCommand(
                mode,
                cityname,
                roadname,
                doornum,
                limitsp,
                nextroad,
                nextdist,
                nextturn,
                camera,
                navidist,
                navitime,
                gpsnum,
                gpsdir
            )

            if(Rx5Handler.rx5 != null) {
                val ok = Rx5Handler.rx5!!.write(cmd)
                if (ok) {
                    viewmodel.log(cmd.toString())
                }else{
                    viewmodel.log("Send command fail, connection is not available")
                }
            }else{
                viewmodel.log("Send command fail, connection is not available")
            }
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(): NaviCommandsFragment {
            return NaviCommandsFragment()
        }
    }
}
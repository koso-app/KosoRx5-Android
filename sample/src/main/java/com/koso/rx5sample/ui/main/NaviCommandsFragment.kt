package com.koso.rx5sample.ui.main

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.koso.rx5.core.Rx5Device
import com.koso.rx5.core.ConnectionService
import com.koso.rx5.core.Rx5Handler
import com.koso.rx5.core.command.NaviInfoCommand
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
        Rx5Handler.STATE_LIVE.observe(this, Observer {
            when (it) {
                Rx5Device.State.Disconnected -> {

                }
                Rx5Device.State.Connected -> {

                }
                Rx5Device.State.Discovering -> {

                }
                Rx5Device.State.Connecting -> {

                }
                else -> {

                }
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewmodel = ViewModelProvider(activity!!).get(TabbedViewModel::class.java)
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
                    viewmodel.log("Failed, connection is not available")
                }
            }else{
                viewmodel.log("Failed, connection is not available")
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
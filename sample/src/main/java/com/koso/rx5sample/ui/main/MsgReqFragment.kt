package com.koso.rx5sample.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.koso.rx5.core.Rx5Handler
import com.koso.rx5.core.command.MsgReqCommand
import com.koso.rx5sample.R
import kotlinx.android.synthetic.main.fragment_msg_req.*


class MsgReqFragment : Fragment() {

    companion object{
        fun newInstance() = MsgReqFragment()
    }

    /**
     * MVVM viewmodel instance
     */
    private lateinit var viewmodel: TabbedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewmodel = ViewModelProvider(requireActivity()).get(TabbedViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_msg_req, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vClear.setOnClickListener{
            val command = MsgReqCommand.ClearCommandBuilder().build()
            val ok = Rx5Handler.rx5!!.write(command)
        }

        vRequst.setOnClickListener{
            val command = MsgReqCommand.ReqCommandBuilder().apply {
                if(vSwitch80.isChecked) this.addItem(MsgReqCommand.PollingItem(0x80, vInput80.text.toString().toInt()))
                if(vSwitch81.isChecked) this.addItem(MsgReqCommand.PollingItem(0x81, vInput81.text.toString().toInt()))
            }.build()

            val ok = Rx5Handler.rx5!!.write(command)
        }
    }

}
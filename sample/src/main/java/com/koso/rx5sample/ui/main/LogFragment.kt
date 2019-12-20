package com.koso.rx5sample.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.koso.rx5sample.R
import kotlinx.android.synthetic.main.fragment_log.*
import java.util.*

class LogFragment : Fragment(){

    companion object{
        fun newInstance(): Fragment {
            return LogFragment()
        }
    }

    private lateinit var viewmodel: TabbedViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewmodel = ViewModelProviders.of(activity!!).get(TabbedViewModel::class.java)


    }

    private fun subscribeLogs() {
        viewmodel.logLiveData.observe(activity as LifecycleOwner, Observer{
            val c = Calendar.getInstance()

            var text = vLog.text.toString()
            text = StringBuilder()
                .appendln("[${c.get(Calendar.HOUR_OF_DAY)}:${c.get(Calendar.MINUTE)}:${c.get(Calendar.SECOND)}] \n$it")
                .appendln(text)
                .toString()
            if(text.length > 10000){
                text = text.substring(0, 9999)
            }
            vLog.setText(text)
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_log, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vClear.setOnClickListener{
            vLog.text = ""
        }
        subscribeLogs()

    }
}
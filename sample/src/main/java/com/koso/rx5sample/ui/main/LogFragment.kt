package com.koso.rx5sample.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.koso.rx5sample.App
import com.koso.rx5sample.R
import com.koso.rx5sample.service.ConnectionService

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_log, container, false)
    }
}
package com.koso.rx5sample.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.koso.rx5sample.R

/**
 * A placeholder fragment containing a simple view.
 */
class NaviCommandsFragment : Fragment() {


    private lateinit var viewmodel: TabbedViewModel

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

    companion object {

        @JvmStatic
        fun newInstance(): NaviCommandsFragment {
            return NaviCommandsFragment()
        }
    }
}
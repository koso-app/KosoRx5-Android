package com.koso.rx5sample.utils

import android.content.Context
import android.content.SharedPreferences
import com.koso.rx5sample.App
import com.koso.rx5sample.R

class SharedPreferenceHandler {
    companion object {
        private const val PARAM_TARGET_MAC_ADDR = "target_mac_address"

        var targetMacAddress: String =
            getSharedPrefences().getString(PARAM_TARGET_MAC_ADDR, "")!!
            set(value) = getEditor().putString(PARAM_TARGET_MAC_ADDR, value).apply()


        /**
         *  To get the default SharedPreferences
         */
        fun getSharedPrefences(): SharedPreferences {
            return App.instance.getSharedPreferences("rx5_pref", Context.MODE_PRIVATE)
        }

        /**
         *  To get the default Editor
         */
        fun getEditor(): SharedPreferences.Editor {
            return getSharedPrefences().edit()
        }


    }
}
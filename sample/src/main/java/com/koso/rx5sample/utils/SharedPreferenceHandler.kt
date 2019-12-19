package com.koso.rx5sample.utils

import android.content.SharedPreferences
import androidx.preference.PreferenceManager

import com.koso.rx5sample.App
import com.koso.rx5sample.R

class SharedPreferenceHandler {
    companion object {
        private const val PARAM_TARGET_MAC_ADDR = "target_mac_address"

        var targetMacAddress: String =
            getSharedPrefences().getString(PARAM_TARGET_MAC_ADDR, App.instance.getString(R.string.default_target_mac_address))!!
            set(value) = getEditor().putString(PARAM_TARGET_MAC_ADDR, value).apply()


        /**
         *  To get the default SharedPreferences
         */
        fun getSharedPrefences(): SharedPreferences {
            return PreferenceManager.getDefaultSharedPreferences(App.instance)
        }

        /**
         *  To get the default Editor
         */
        fun getEditor(): SharedPreferences.Editor {
            return getSharedPrefences().edit()
        }


    }
}
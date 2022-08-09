package com.koso.rx5sample.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class TabbedViewModel : ViewModel() {

    private val _logLive = MutableLiveData<String>()

    val logLiveData: LiveData<String> = _logLive

    fun log(text: String){
        _logLive.value = text
    }
}
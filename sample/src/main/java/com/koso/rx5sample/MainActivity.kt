package com.koso.rx5sample

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.koso.core.BaseBluetoothDevice
import com.koso.core.Rx5
import com.koso.core.command.NaviInfoCommand
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val REQUEST_ENABLE_BT = 10
    private val REQUEST_PERMISSION_COARSE_LOCATION = 20

    private val rx5 = Rx5(this)

    val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()

        subscribeStateEvent()
        subscribeDevices()

        checkLocationPermission()
    }

    private fun subscribeStateEvent() {
        rx5.stateLive.observe(this, Observer{
            when(it){
                BaseBluetoothDevice.State.Disconnected -> {
                    vState.setText(R.string.disconnected)
                }
                BaseBluetoothDevice.State.Connected -> {
                    vState.setText(R.string.connected)
                    val cmd =
                        NaviInfoCommand(0, "tainan", "abc road", "17a", 40, "simeng road", 350, 5, 10, 35000, 40, 12, 120)

                    rx5.write(cmd)
                }
                BaseBluetoothDevice.State.Discovering -> {
                    vState.setText(R.string.discovering)
                }
                BaseBluetoothDevice.State.Connecting -> {
                    vState.setText(R.string.connecting)
                }
                else ->{

                }
            }
        })
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_PERMISSION_COARSE_LOCATION
            )
        }
    }


    private fun subscribeDevices() {
        val disposable = rx5.observeDevices()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.computation())
            .subscribe({
                it?.let {

                    Log.d("rx5debug", "${it.name} ${it.address} found")
                    if (it.name == "Koso-BT") {
                        rx5.cancelDiscovery()
                        rx5.connectAsClient(it)
                        vState.text = "${it.name} found"
                    }
                }
            }, {
                it.printStackTrace()
            })

        compositeDisposable.add(disposable)
    }




    private fun initViews() {
        vStart.setOnClickListener {
            if (rx5.stateLive.value == BaseBluetoothDevice.State.Connected ||
                rx5.stateLive.value == BaseBluetoothDevice.State.Connecting) return@setOnClickListener

            if (!rx5.isBluetoothAvailable()) {
                // handle the lack of bluetooth support
            } else {
                // check if bluetooth is currently enabled and ready for use
                if (!rx5.isBluetoothEnabled()) {
                    // to enable bluetooth via startActivityForResult()
                    rx5.enableBluetooth(this, REQUEST_ENABLE_BT);
                } else {


                    rx5.startDiscovery()
                }
            }


        }
    }


    override fun onDestroy() {
        super.onDestroy()
        rx5.destory()
        compositeDisposable.clear()
    }
}

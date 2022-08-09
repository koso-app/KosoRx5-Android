package com.koso.rx5sample.utils

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity

class BluetoothUtil {
    companion object{
        fun checkAndRequestBluetooth(activity: AppCompatActivity, REQUEST_ENABLE_BT: Int): Boolean{
            val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter() ?: return false

            if (!bluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                return false
            }

            return true
        }

        fun isLocationServiceEnabled(context: Context): Boolean {
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
        }


    }
}
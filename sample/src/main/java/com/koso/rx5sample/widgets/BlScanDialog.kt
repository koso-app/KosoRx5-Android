package com.koso.rx5sample.widgets

import android.app.AlertDialog
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.koso.rx5sample.R
import com.koso.rx5sample.utils.SharedPreferenceHandler


class BlScanDialog : DialogFragment() {

    var vRecycler: RecyclerView? = null
    val adapter = BlDeviceListAdapter()
    var bluetoothAdapter: BluetoothAdapter? = null


    // Create a BroadcastReceiver for ACTION_FOUND.
    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val deviceName = device.name
                    if (deviceName == null || deviceName.isEmpty()) return
                    adapter.add(device)
                }
            }
        }
    }




    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_bl_scan_dialog, null)
        vRecycler = view.findViewById(R.id.vList)
        vRecycler?.layoutManager = LinearLayoutManager(context)
        vRecycler?.adapter = adapter

        val builder = AlertDialog.Builder(context)
            .setTitle(R.string.scanning)
            .setView(view)
            .setPositiveButton(android.R.string.cancel) { d, which ->

            }


        scan()

        return builder.create()
    }



    private fun scan() {
        // Register for broadcasts when a device is discovered.
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        activity?.registerReceiver(receiver, filter)
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val success = bluetoothAdapter?.startDiscovery()
        Log.d("BlScanDialog", "scan: ${success}")
    }

    override fun onDetach() {
        super.onDetach()

    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().unregisterReceiver(receiver)
    }

    inner class BlDeviceListAdapter :
        RecyclerView.Adapter<DeviceViewHolder>() {
        val list = mutableListOf<BluetoothDevice>()

        fun add(d: BluetoothDevice) {
            var found = false
            for (device in list) {
                if (device.address == d.address) {
                    found = true
                    break
                }
            }

            if (!found) {
                list.add(d)
                notifyDataSetChanged()
            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
            val v =
                LayoutInflater.from(parent.context).inflate(R.layout.layout_ble_item, parent, false)
            return DeviceViewHolder(v)
        }

        override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
            holder.bind(list[position])
        }

        override fun getItemCount(): Int {
            return list.size
        }

    }

    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var device: BluetoothDevice? = null

        init {
            itemView.findViewById<ViewGroup>(R.id.vRoot).setOnClickListener {
                SharedPreferenceHandler.targetMacAddress = device!!.address
                dialog?.dismiss()
            }
        }

        fun bind(d: BluetoothDevice) {
            device = d
            itemView.findViewById<TextView>(R.id.vName).text = d.name
            itemView.findViewById<TextView>(R.id.vAddress).text = d.address
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        bluetoothAdapter?.cancelDiscovery()
    }
}
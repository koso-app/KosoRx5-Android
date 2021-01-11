package com.koso.rx5sample.widgets

import android.app.AlertDialog
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.*
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
    var listener: BlSelectListener? = null


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
                    if (possibleDevice(device)) {
                        val deviceName = device.name
                        if (deviceName == null || deviceName.isEmpty()) return
                        adapter.addFound(device)
                    }
                }
            }
        }
    }

    interface BlSelectListener{
        fun  onSelect()
    }

    fun setBlSelectListener(l: BlSelectListener): BlScanDialog{
        listener = l
        return this
    }

    private fun possibleDevice(device: BluetoothDevice): Boolean {

//        return device.type == BluetoothDevice.DEVICE_TYPE_CLASSIC
        return true
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
        bound()


        return builder.create()
    }


    private fun bound() {
        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
        pairedDevices?.filter { d -> possibleDevice(d) }
            ?.forEach { device ->
                adapter.addBond(device)
            }
    }

    private fun scan() {
        // Register for broadcasts when a device is discovered.
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        activity?.registerReceiver(receiver, filter)
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val success = bluetoothAdapter?.startDiscovery()
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
        val foundList = mutableListOf<BluetoothDevice>()
        val bondList = mutableListOf<BluetoothDevice>()

        fun addBond(d: BluetoothDevice) {
            var found = false
            for (device in bondList) {
                if (device.address == d.address) {
                    found = true
                    break
                }
            }


            if (!found) {
                bondList.add(d)
                notifyDataSetChanged()
            }
        }

        fun addFound(d: BluetoothDevice) {
            var found = false
            for (device in foundList) {
                if (device.address == d.address) {
                    found = true
                    break
                }
            }


            if (!found) {
                foundList.add(d)
                notifyDataSetChanged()
            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
            val v =
                LayoutInflater.from(parent.context).inflate(R.layout.layout_ble_item, parent, false)
            return DeviceViewHolder(v)
        }

        override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
            if (position < foundList.size) {
                holder.bind(foundList[position], true)
            } else {
                holder.bind(bondList[position - foundList.size], false)
            }
        }

        override fun getItemCount(): Int {
            return foundList.size + bondList.size
        }

    }

    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var device: BluetoothDevice? = null

        init {
            itemView.findViewById<ViewGroup>(R.id.vRoot).setOnClickListener {
                SharedPreferenceHandler.targetMacAddress = device!!.address
                listener?.onSelect()
                dialog?.dismiss()
            }
        }

        fun bind(d: BluetoothDevice, found: Boolean) {
            device = d
            itemView.findViewById<TextView>(R.id.vName).text = d.name
            itemView.findViewById<TextView>(R.id.vAddress).text = d.address
            itemView.findViewById<TextView>(R.id.vName)
                .setTextColor(if (found) Color.BLACK else Color.LTGRAY)
            itemView.findViewById<TextView>(R.id.vAddress)
                .setTextColor(if (found) Color.DKGRAY else Color.LTGRAY)
            itemView.findViewById<TextView>(R.id.vAddress).textSize = 10f
            itemView.findViewById<TextView>(R.id.vType).text = when(d.type) {
                BluetoothDevice.DEVICE_TYPE_CLASSIC -> "CLASSIC"
                BluetoothDevice.DEVICE_TYPE_LE -> "BLE"
                BluetoothDevice.DEVICE_TYPE_DUAL -> "DUAL"
                else -> "UNKNOW"
            }
            itemView.findViewById<TextView>(R.id.vType).textSize = 10f
            itemView.findViewById<ImageView>(R.id.vIcon).setImageResource(getIcon(d.bluetoothClass.majorDeviceClass))
        }

        fun getIcon(type: Int): Int{
            when(type){
                2304 -> return R.drawable.ic_baseline_health_and_safety_24
                2048 -> return R.drawable.ic_baseline_toys_24
                1792 -> return R.drawable.ic_speed_meter
                1280 -> return R.drawable.ic_baseline_adjust_24
                1024 -> return R.drawable.ic_baseline_slow_motion_video_24
                512 -> return R.drawable.ic_baseline_smartphone_24
                256 -> return R.drawable.ic_baseline_computer_24
                else -> return R.drawable.ic_baseline_bluetooth_24
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        bluetoothAdapter?.cancelDiscovery()
    }
}
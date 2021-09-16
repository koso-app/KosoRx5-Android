package com.koso.rx5sample.ui.main

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.android.volley.Response
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.Volley
import com.koso.rx5.core.Rx5Handler
import com.koso.rx5.core.command.outgoing.StaticMapCommand
import com.koso.rx5sample.R
import kotlinx.android.synthetic.main.fragment_static_map.*
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer


/**
 * A simple [Fragment] subclass.
 * Use the [StaticMapFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StaticMapFragment : Fragment() {
    private lateinit var viewmodel: TabbedViewModel
    val bitmapWidth = 242
    val bitmapHeith = 282
    var quality = 80
    @SuppressLint("SetTextI18n")
    private val responseListener = Response.Listener<Bitmap> {


        val stream = ByteArrayOutputStream()

        it.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        val bytes = stream.toByteArray()
        vImage.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
        vState.text = "${bytes.count()} Bytes (quality: $quality%)"
        val cmds = StaticMapCommand.createFromBytes(bytes)

        if(Rx5Handler.rx5 != null) {
            cmds.forEach { cmd ->
                val ok = Rx5Handler.rx5!!.write(cmd)
                if (ok) {
                    viewmodel.log(cmd.toString())

                } else {
                    val msg = "Failed, connection is not available"
                    viewmodel.log(msg)
                }
            }
        }else{
            val msg = "Failed, connection is not available"
            viewmodel.log(msg)
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }
    }

    private val errorListener = Response.ErrorListener {
        vState.text = it.message
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewmodel = ViewModelProvider(requireActivity()).get(TabbedViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_static_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        vButton.setOnClickListener{
            val url = "https://maps.googleapis.com/maps/api/staticmap?center=22.992461322797763,120.20515240183124&zoom=15&format=jpg&size=242x282&maptype=roadmap&markers=color:blue%7Clabel:S%7C22.994947455674406,120.20556867649405&markers=color:green%7Clabel:G%7C22.98997518992112,120.20473612716843&key=AIzaSyBmsJg-P3oQVgdEkP3O8f7jEuF0cSnQNXE&path=weight:3%7Ccolor:red%7Cenc:mejkCyst|UbFp@jAJpANAT@ZHZPTVLFBF@`@@RCXWVg@BKl@B|EZxCPH?"
            fetchStaticMap(url)
            vState.text = "請求中"
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                quality = progress * 10
                vQuality.text = "quality = $quality"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })
    }

    private fun fetchStaticMap(url: String) {

        val queue = Volley.newRequestQueue(requireContext())
        val imageRequest = ImageRequest(url, responseListener, bitmapWidth, bitmapHeith, ImageView.ScaleType.FIT_START, Bitmap.Config.RGB_565, errorListener)
        queue.add(imageRequest)
    }

    companion object {

        @JvmStatic
        fun newInstance() =
            StaticMapFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}
package com.koso.rx5sample.ui.main

import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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


/**
 * A simple [Fragment] subclass.
 * Use the [StaticMapFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StaticMapFragment : Fragment() {
    private lateinit var viewmodel: TabbedViewModel
    val bitmapWidth = 242
    val bitmapHeith = 282

    private val responseListener = Response.Listener<Bitmap> {

        vImage.setImageBitmap(it)
        vState.text = "${it.byteCount} Bytes"

        val bos = ByteArrayOutputStream()
        it.compress(Bitmap.CompressFormat.JPEG, 100, bos)
        val bites = bos.toByteArray()
        val cmd = StaticMapCommand(bites)

        if(Rx5Handler.rx5 != null) {
            val ok = Rx5Handler.rx5!!.write(cmd)
            if (ok) {
                viewmodel.log(cmd.toString())
                Toast.makeText(requireContext(), "已送出圖片", Toast.LENGTH_SHORT).show()
            }else{
                val msg = "Failed, connection is not available"
                viewmodel.log(msg)
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
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
            val url = "https://maps.googleapis.com/maps/api/staticmap?center=22.992461322797763,120.20515240183124&zoom=15&size=242x282&maptype=roadmap&markers=color:blue%7Clabel:S%7C22.994947455674406,120.20556867649405&markers=color:green%7Clabel:G%7C22.98997518992112,120.20473612716843&key=AIzaSyBmsJg-P3oQVgdEkP3O8f7jEuF0cSnQNXE&path=weight:3%7Ccolor:red%7Cenc:mejkCyst|UbFp@jAJpANAT@ZHZPTVLFBF@`@@RCXWVg@BKl@B|EZxCPH?"
            fetchStaticMap(url)
            vState.text = "請求中"
        }
    }

    private fun fetchStaticMap(url: String) {

        val queue = Volley.newRequestQueue(requireContext())
        val imageRequest = ImageRequest(url, responseListener, bitmapWidth, bitmapHeith, ImageView.ScaleType.CENTER, Bitmap.Config.ARGB_8888, errorListener)
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
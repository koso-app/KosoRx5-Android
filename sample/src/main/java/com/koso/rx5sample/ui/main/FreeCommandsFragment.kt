package com.koso.rx5sample.ui.main

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.koso.rx5.core.Rx5Handler
import com.koso.rx5sample.R
import com.koso.rx5sample.utils.StringUtils
import kotlinx.android.synthetic.main.fragment_free.*
import java.lang.Exception

class FreeCommandsFragment : Fragment() {


    companion object{
        fun newInstance(): FreeCommandsFragment{
            return FreeCommandsFragment()
        }
    }
    /**
     * MVVM viewmodel instance
     */
    private lateinit var viewmodel: TabbedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewmodel = ViewModelProvider(requireActivity()).get(TabbedViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_free, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        vSend.setOnClickListener {
            val content = vEditText.text.toString()
            if(content.isEmpty()){
                Toast.makeText(context, "請輸入內容", Toast.LENGTH_SHORT).show()
            }else {
                try {
                    val bytes = StringUtils.hexStringToByteArray(content)

                    if (Rx5Handler.rx5 != null) {
                        val ok = Rx5Handler.rx5!!.write(bytes)
                        if (ok) {
                            viewmodel.log("$content")
                        } else {
                            viewmodel.log("Failed, connection is not available")
                        }
                    } else {
                        viewmodel.log("Failed, connection is not available")
                    }
                }catch (e: Exception){
                    Toast.makeText(context, "請輸入16進位內容", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
        }

        vEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }
}
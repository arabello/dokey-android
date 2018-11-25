package io.rocketguys.dokey.connect.usb

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.rocketguys.dokey.R
import kotlinx.android.synthetic.main.fragment_usb_instruction.view.*

private const val ARG_PAGE_INDEX = "arg_page_index"

class USBInstructionFragment : Fragment() {
    private var argPageIndex: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            argPageIndex = it.getInt(ARG_PAGE_INDEX)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val frag = inflater.inflate(R.layout.fragment_usb_instruction, container, false)

        when(argPageIndex){
            0 -> {
                frag.introFragDesc.text = getString(R.string.frag_usb_instruction_0)
                frag.introFragImg.setImageResource(R.drawable.usb_instruction_0)
            }
            1 -> {
                frag.introFragDesc.text = getString(R.string.frag_usb_instruction_1)
                frag.introFragImg.setImageResource(R.drawable.usb_instruction_1)
            }
            2 -> {
                frag.introFragDesc.text = getString(R.string.frag_usb_instruction_2)
                frag.introFragImg.setImageResource(R.drawable.usb_instruction_2)
            }
        }

        return frag
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: Int) =
                USBInstructionFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_PAGE_INDEX, param1)
                    }
                }
    }
}

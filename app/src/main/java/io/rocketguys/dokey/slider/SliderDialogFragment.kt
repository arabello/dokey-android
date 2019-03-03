package io.rocketguys.dokey.slider

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import io.rocketguys.dokey.R


class SliderDialogFragment : DialogFragment() {

    companion object {
        fun newInstance(title: String): SliderDialogFragment {
            val frag = SliderDialogFragment()
            val args = Bundle()
            args.putString("title", title)
            frag.arguments = args
            return frag
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { acty ->

            val title = arguments?.getString("title")
            val builder = AlertDialog.Builder(acty, R.style.SliderDialog)
            val inflater = requireActivity().layoutInflater

            builder.setTitle(title)
            builder.setView(inflater.inflate(R.layout.slider, null))
            builder.create()

        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
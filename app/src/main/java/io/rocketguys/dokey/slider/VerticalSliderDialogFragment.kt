package io.rocketguys.dokey.slider

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.Gravity
import android.view.WindowManager
import io.rocketguys.dokey.R


class VerticalSliderDialogFragment : DialogFragment(), SliderView {

    companion object {
        const val GRAVITY_END = Gravity.END
        const val GRAVITY_START = Gravity.START

        private const val ARGS_TITLE = "title"
        private const val ARGS_GRAVITY = "gravity"

        fun newInstance(title: String, gravity: Int): VerticalSliderDialogFragment{
            val frag = VerticalSliderDialogFragment()
            val args = Bundle()
            args.putString(ARGS_TITLE, title)
            args.putInt(ARGS_GRAVITY, gravity)
            frag.arguments = args
            return frag
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { acty ->

            val title = arguments?.getString(ARGS_TITLE)
            val builder = AlertDialog.Builder(acty, R.style.SliderDialog)
            val inflater = requireActivity().layoutInflater
            val inflated = inflater.inflate(R.layout.slider_vertical, null)

            builder.setTitle(title)
            builder.setView(inflated)
            val dialog = builder.create()

            val window = dialog.window
            val layoutParams = window?.attributes

            layoutParams?.gravity = arguments?.getInt(VerticalSliderDialogFragment.ARGS_GRAVITY)
            layoutParams?.flags = layoutParams?.flags?.and((WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()))

            window?.attributes = layoutParams

            dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }


    override fun onDataChange(viewModel: SliderViewModel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
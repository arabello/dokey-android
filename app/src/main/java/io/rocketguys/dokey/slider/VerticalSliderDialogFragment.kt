package io.rocketguys.dokey.slider

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TableLayout
import io.rocketguys.dokey.R


class VerticalSliderDialogFragment : DialogFragment(), SliderView {
    private var empty: View ?= null
    private var fill: View  ?= null
    private var unrendered : SliderViewModel ?= null

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

            fill = inflated.findViewById<ImageView>(R.id.slider_fill)
            empty = inflated.findViewById<ImageView>(R.id.slider_empty)
            unrendered?.let { onDataChange(it) }

            builder.setTitle(title)
            builder.setView(inflated)

            val dialog = builder.create()
            val window = dialog.window
            val layoutParams = window?.attributes

            layoutParams?.gravity = arguments?.getInt(VerticalSliderDialogFragment.ARGS_GRAVITY)
            layoutParams?.flags = layoutParams?.flags?.and((WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv())) //TODO fix

            window?.attributes = layoutParams


            dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }


    override fun onDataChange(viewModel: SliderViewModel) {
        if (fill != null) fill?.let{
            unrendered = null
            it.layoutParams = TableLayout.LayoutParams(it.layoutParams.width, it.layoutParams.height, viewModel.value)
        }else{
            unrendered = viewModel
        }

        empty?.let {
            it.layoutParams = TableLayout.LayoutParams(it.layoutParams.width, it.layoutParams.height, 1f - viewModel.value)
        }
    }
}
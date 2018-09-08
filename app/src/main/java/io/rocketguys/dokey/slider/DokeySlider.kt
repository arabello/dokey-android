package io.rocketguys.dokey.slider

import android.app.Activity
import android.os.Build
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import io.rocketguys.dokey.R
import jp.wasabeef.blurry.Blurry

/**
 * TODO: Add class description
 *
 * @author Matteo Pellegrino matteo.pelle.pellegrino@gmail.com
 */
class DokeySlider (val activity: Activity) :
        PopupWindow(activity.layoutInflater.inflate(R.layout.dokey_slider, null),
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true){

    var rootView: ViewGroup? = activity.window.decorView.findViewById(android.R.id.content)

    companion object {
        val BLUR_RADIUS = 8
        val BLUR_SAMPLING = 1
        val BLUR_ANIMATE_TIME = 90
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            elevation = 25f
    }

    fun show(){

        Blurry.with(activity)
                .radius(BLUR_RADIUS)
                .sampling(BLUR_SAMPLING)
                .async()
                .animate(BLUR_ANIMATE_TIME)
                .onto(rootView)

        showAtLocation(rootView, Gravity.CENTER, 0, 0)
    }

    override fun setOnDismissListener(onDismissListener: OnDismissListener?) {
        Blurry.delete(rootView)
        super.setOnDismissListener(onDismissListener)
    }
}
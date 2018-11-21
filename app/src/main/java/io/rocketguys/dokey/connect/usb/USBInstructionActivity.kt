package io.rocketguys.dokey.connect.usb

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageButton
import io.rocketguys.dokey.R
import kotlinx.android.synthetic.main.activity_intro.*

class USBInstructionActivity : AppCompatActivity() {

    companion object {
        const val ANIM_DURATION = 240L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usb_instruction)

        introViewPager.adapter = USBInstructionAdapter(supportFragmentManager)

        prevBtn.visibility = View.INVISIBLE

        introViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(p0: Int){}

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {}

            override fun onPageSelected(position: Int) {
                when(position){
                    0 -> {
                        prevBtn.hide()
                        nextBtn.reveal()
                    }
                    introViewPager.adapter?.count!! - 1 -> {
                        prevBtn.reveal()
                        nextBtn.hide()
                    }
                    else -> {
                        prevBtn.reveal()
                        nextBtn.reveal()
                    }
                }
            }
        })

        prevBtn.setOnClickListener {
            if (introViewPager.currentItem > 0)
                introViewPager.setCurrentItem(introViewPager.currentItem -1, true)
        }

        nextBtn.setOnClickListener {
            if (introViewPager.currentItem < introViewPager.adapter?.count!!)
                introViewPager.setCurrentItem(introViewPager.currentItem + 1, true)
        }
    }

    fun onIntroCompleted(){
        startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
        finish()
    }

    fun ImageButton.reveal(){
        if (visibility != View.VISIBLE){
            alpha = 0f
            visibility = View.VISIBLE
            animate().setDuration(ANIM_DURATION).alpha(1f).start()
        }
    }

    fun ImageButton.hide(){
        if (visibility != View.INVISIBLE){
            alpha = 1f
            animate().setDuration(ANIM_DURATION).withEndAction {
                visibility = View.INVISIBLE
            }.alpha(0f).start()
        }
    }
}

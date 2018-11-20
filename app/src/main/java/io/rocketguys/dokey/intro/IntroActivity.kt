package io.rocketguys.dokey.intro

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageButton
import io.rocketguys.dokey.R
import io.rocketguys.dokey.connect.ConnectActivity
import kotlinx.android.synthetic.main.activity_intro.*

class IntroActivity : AppCompatActivity() {

    companion object {
        const val FIRST_RUN_KEY  = "first_run"
        const val ANIM_DURATION = 240L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        introViewPager.adapter = IntroAdapter(supportFragmentManager)

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
        val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        pref.edit().putBoolean(FIRST_RUN_KEY, false).apply() // async
        val intent = Intent(this, ConnectActivity::class.java)
        intent.putExtra(ConnectActivity.EXTRA_FIRST_LAUNCH, true)
        startActivity(intent)
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

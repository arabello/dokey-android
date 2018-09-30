package io.rocketguys.dokey.intro

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import io.rocketguys.dokey.R
import io.rocketguys.dokey.connect.ConnectActivity
import io.rocketguys.dokey.connect.ScanActivity
import kotlinx.android.synthetic.main.activity_intro.*

class IntroActivity : AppCompatActivity() {

    companion object {
        const val FIRST_RUN_KEY  = "first_run"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        introViewPager.adapter = IntroAdapter(supportFragmentManager)
    }

    fun onIntroCompleted(){
        val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        pref.edit().putBoolean(FIRST_RUN_KEY, false).apply() // async
        val intent = Intent(this, ConnectActivity::class.java)
        intent.putExtra(ConnectActivity.EXTRA_FORCE_SCAN, true)
        startActivity(intent)
        finish()
    }
}

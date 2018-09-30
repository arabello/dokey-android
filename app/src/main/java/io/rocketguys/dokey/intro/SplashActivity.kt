package io.rocketguys.dokey.intro

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.preference.PreferenceManager
import io.rocketguys.dokey.connect.ConnectActivity


class SplashActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val isFirstRun = pref.getBoolean(IntroActivity.FIRST_RUN_KEY, true)
        lateinit var intent: Intent

        intent = if (isFirstRun)
            Intent(this, IntroActivity::class.java)
        else
            Intent(this, ConnectActivity::class.java)

        startActivity(intent)
        finish()
    }
}

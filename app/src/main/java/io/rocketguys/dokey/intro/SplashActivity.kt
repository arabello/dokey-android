package io.rocketguys.dokey.intro

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import io.rocketguys.dokey.connect.ConnectActivity


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, ConnectActivity::class.java)
        startActivity(intent)
        finish()
    }
}

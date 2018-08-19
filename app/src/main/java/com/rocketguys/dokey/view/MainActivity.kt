package com.rocketguys.dokey.view

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import com.rocketguys.dokey.R
import kotlinx.android.synthetic.main.activity_home.*

class MainActivity : AppCompatActivity() {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                message.setText(R.string.title_home)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_shortcut -> {
                message.setText(R.string.title_shortcut)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_system -> {
                message.setText(R.string.title_system)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_more -> {
                message.setText(R.string.title_more)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }
}

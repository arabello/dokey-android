package com.rocketguys.dokey.view

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import com.rocketguys.dokey.R
import kotlinx.android.synthetic.main.activity_home.*

class MainActivity : AppCompatActivity() {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->

        if (item.itemId != R.id.navigation_more)
        with(navigation.menu){
            findItem(R.id.navigation_home).setIcon(R.drawable.ic_outline_grid_on)
            findItem(R.id.navigation_shortcut).setIcon(R.drawable.ic_outline_keyboard)
            findItem(R.id.navigation_system).setIcon(R.drawable.ic_outline_computer)
        }

        when (item.itemId) {
            R.id.navigation_home -> {
                message.setText(R.string.title_home)
                item.setIcon(R.drawable.ic_outline_grid_on_grad_1)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_shortcut -> {
                message.setText(R.string.title_shortcut)
                item.setIcon(R.drawable.ic_outline_keyboard_grad_2)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_system -> {
                message.setText(R.string.title_system)
                item.setIcon(R.drawable.ic_outline_computer_grad_3)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_more -> {
                return@OnNavigationItemSelectedListener false
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        navigation.itemIconTintList = null
    }
}

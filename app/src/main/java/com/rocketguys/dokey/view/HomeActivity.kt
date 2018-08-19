package com.rocketguys.dokey.view

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import com.rocketguys.dokey.R
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.toolbar.*

class HomeActivity : AppCompatActivity() {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->

        if (item.itemId == R.id.navigation_more)
            return@OnNavigationItemSelectedListener false

        with(navigation.menu){
            findItem(R.id.navigation_launchpad).setIcon(R.drawable.ic_outline_grid_on)
            findItem(R.id.navigation_shortcut).setIcon(R.drawable.ic_outline_keyboard)
            findItem(R.id.navigation_system).setIcon(R.drawable.ic_outline_computer)
        }

        when (item.itemId) {
            R.id.navigation_launchpad -> {
                item.setIcon(R.drawable.ic_outline_grid_on_grad_1)
                toolbar.setTitle(R.string.title_launchpad)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_shortcut -> {
                item.setIcon(R.drawable.ic_outline_keyboard_grad_2)
                // TODO toolbar.setTitle(focusedApp.getTitle())
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_system -> {
                item.setIcon(R.drawable.ic_outline_computer_grad_3)
                toolbar.setTitle(R.string.title_system)
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
        setSupportActionBar(toolbar)

        // Init BottomNavigationView
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        navigation.itemIconTintList = null
        navigation.selectedItemId = R.id.navigation_launchpad
        toolbar.setTitle(R.string.title_launchpad)
    }
}

package io.rocketguys.dokey.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import io.rocketguys.dokey.R
import io.rocketguys.dokey.adapter.ActiveAppAdapter
import io.rocketguys.dokey.adapter.ActiveAppMock
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.toolbar.*
import android.graphics.drawable.TransitionDrawable
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView


class HomeActivity : AppCompatActivity() {
    companion object {
        const val ACTIVE_APPS_BG_TRANS_DURATION = 400
    }

    val mActiveAppAdapter = ActiveAppAdapter(ArrayList())

    private fun RecyclerView.transBackgroundTo(newBackground: Drawable, duration: Int){
        val start = if (this.background == null) ColorDrawable(Color.TRANSPARENT) else this.background
        val bgTrans = TransitionDrawable(arrayOf(start, newBackground))
        bgTrans.isCrossFadeEnabled = true
        this.background = bgTrans
        bgTrans.startTransition(duration)
    }

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
                recyclerView.transBackgroundTo(ContextCompat.getDrawable(applicationContext, R.color.grad_1)!!, ACTIVE_APPS_BG_TRANS_DURATION)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_shortcut -> {
                item.setIcon(R.drawable.ic_outline_keyboard_grad_2)
                // TODO toolbar.setTitle(focusedApp.getTitle())
                recyclerView.transBackgroundTo(ContextCompat.getDrawable(applicationContext, R.color.grad_2)!!, ACTIVE_APPS_BG_TRANS_DURATION)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_system -> {
                item.setIcon(R.drawable.ic_outline_computer_grad_3)
                toolbar.setTitle(R.string.title_system)
                recyclerView.transBackgroundTo(ContextCompat.getDrawable(applicationContext, R.color.grad_3)!!, ACTIVE_APPS_BG_TRANS_DURATION)
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

        // Init RecyclerView for active apps
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = mActiveAppAdapter
        mActiveAppAdapter.activeApps = ActiveAppMock.Factory.list(baseContext, 9)
        mActiveAppAdapter.notifyDataSetChanged()
    }
}

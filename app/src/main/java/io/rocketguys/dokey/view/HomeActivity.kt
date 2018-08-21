package io.rocketguys.dokey.view

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.BottomNavigationView
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import io.rocketguys.dokey.R
import io.rocketguys.dokey.adapter.ActiveAppAdapter
import io.rocketguys.dokey.adapter.ActiveAppMock
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.toolbar.*


class HomeActivity : AppCompatActivity() {
    companion object {
        const val DRAWABLE_GRAD_TRANS_DURATION = 360
    }

    val mActiveAppAdapter = ActiveAppAdapter(ArrayList())

    enum class LOCK{ INVISIBLE, CLOSE, OPEN}
    var lockState = LOCK.CLOSE

    private fun MenuItem.transIconTo(newIcon: Drawable, duration: Int){
        val bgTrans = TransitionDrawable(arrayOf(this.icon, newIcon))
        bgTrans.isCrossFadeEnabled = true
        this.icon = bgTrans
        bgTrans.startTransition(duration)
    }

    private fun MenuItem.transStateTo(newState: LOCK, duration: Int){
        if (this.itemId != R.id.action_lock)
            return

        lateinit var trans: TransitionDrawable

        when(newState){
            HomeActivity.LOCK.INVISIBLE -> {
                trans = TransitionDrawable(arrayOf(this.icon, ColorDrawable(Color.TRANSPARENT)))
                Handler().postDelayed({
                    runOnUiThread {
                        this.isVisible = false
                    }
                }, DRAWABLE_GRAD_TRANS_DURATION.toLong())
            }
            HomeActivity.LOCK.CLOSE -> {
                this.isVisible = true
                trans = TransitionDrawable(arrayOf(this.icon, ContextCompat.getDrawable(baseContext, R.drawable.ic_lock_grad_2)))
            }
            HomeActivity.LOCK.OPEN -> {
                this.isVisible = true
                trans = TransitionDrawable(arrayOf(this.icon, ContextCompat.getDrawable(baseContext, R.drawable.ic_lock_open_grad_2)))
            }
        }

        trans.isCrossFadeEnabled = true
        this.icon = trans
        trans.startTransition(duration)
    }

    private fun RecyclerView.transBackgroundTo(newBackground: Drawable, duration: Int){
        val start = if (this.background == null) ColorDrawable(Color.TRANSPARENT) else this.background
        val bgTrans = TransitionDrawable(arrayOf(start, newBackground))
        bgTrans.isCrossFadeEnabled = true
        this.background = bgTrans
        bgTrans.startTransition(duration)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        toolbar.inflateMenu(R.menu.toolbar)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_edit -> {
            Toast.makeText(baseContext, "action_edit", Toast.LENGTH_SHORT).show()
            true
        }

        R.id.action_lock -> {
            lockState = if (lockState == LOCK.OPEN) LOCK.CLOSE else LOCK.OPEN
            item.transStateTo(lockState, DRAWABLE_GRAD_TRANS_DURATION)
            Toast.makeText(baseContext, "action_lock $lockState", Toast.LENGTH_SHORT).show()
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
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
                // Active apps
                recyclerView?.transBackgroundTo(ContextCompat.getDrawable(baseContext, R.color.grad_1)!!, DRAWABLE_GRAD_TRANS_DURATION)

                // NavigationBottomView
                item.setIcon(R.drawable.ic_outline_grid_on_grad_1)

                // Toolbar
                toolbar.setTitle(R.string.title_launchpad)
                toolbar.menu.findItem(R.id.action_edit)?.transIconTo(ContextCompat.getDrawable(baseContext, R.drawable.ic_edit_grad_1)!!, DRAWABLE_GRAD_TRANS_DURATION)
                toolbar.menu.findItem(R.id.action_lock)?.transStateTo(LOCK.INVISIBLE, DRAWABLE_GRAD_TRANS_DURATION)

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_shortcut -> {
                // Active apps
                recyclerView?.transBackgroundTo(ContextCompat.getDrawable(baseContext, R.color.grad_2)!!, DRAWABLE_GRAD_TRANS_DURATION)

                // NavigationBottomView
                item.setIcon(R.drawable.ic_outline_keyboard_grad_2)

                // Toolbar
                // TODO toolbar.setTitle(focusedApp.getTitle())
                toolbar.menu.findItem(R.id.action_edit)?.transIconTo(ContextCompat.getDrawable(baseContext, R.drawable.ic_edit_grad_2)!!, DRAWABLE_GRAD_TRANS_DURATION)
                toolbar.menu.findItem(R.id.action_lock)?.transStateTo(lockState, DRAWABLE_GRAD_TRANS_DURATION)

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_system -> {
                // Active apps
                recyclerView?.transBackgroundTo(ContextCompat.getDrawable(baseContext, R.color.grad_3)!!, DRAWABLE_GRAD_TRANS_DURATION)

                // NavigationBottomView
                item.setIcon(R.drawable.ic_outline_computer_grad_3)

                // Toolbar
                toolbar.setTitle(R.string.title_system)
                toolbar.menu.findItem(R.id.action_edit)?.transIconTo(ContextCompat.getDrawable(baseContext, R.drawable.ic_edit_grad_3)!!, DRAWABLE_GRAD_TRANS_DURATION)
                toolbar.menu.findItem(R.id.action_lock)?.transStateTo(LOCK.INVISIBLE, DRAWABLE_GRAD_TRANS_DURATION)

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

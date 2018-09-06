package io.rocketguys.dokey.view.activity

import android.content.Intent
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
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.*
import android.widget.Toast
import io.matteopellegrino.pagedgrid.adapter.GridAdapter
import io.rocketguys.dokey.GridMock
import io.rocketguys.dokey.R
import io.rocketguys.dokey.adapter.ActiveAppAdapter
import io.rocketguys.dokey.adapter.ActiveAppMock
import jp.wasabeef.blurry.Blurry
import kotlinx.android.synthetic.main.activity_home.*
import android.view.Gravity
import android.widget.PopupWindow
import android.widget.LinearLayout
import io.rocketguys.dokey.view.DokeySlider


class HomeActivity : AppCompatActivity(), PopupMenu.OnMenuItemClickListener {
    companion object {
        const val DRAWABLE_GRAD_TRANS_DURATION = 420
    }

    val mActiveAppAdapter = ActiveAppAdapter(ArrayList())
    val mGridAdapter = GridAdapter(arrayOf())
    lateinit var mToolbar: Toolbar

    enum class LOCK{ INVISIBLE, CLOSE, OPEN}
    var lockState = LOCK.CLOSE

    // Transition animation to change Active Apps RecyclerView background
    private fun View.transBackgroundTo(newBackground: Drawable, duration: Int){
        val start = if (this.background == null) ColorDrawable(Color.TRANSPARENT) else this.background
        val crossfader = TransitionDrawable(arrayOf(start, newBackground))
        this.background = crossfader
        crossfader.startTransition(duration)
    }

    // Transition animation to change action icons in the mToolbar
    private fun MenuItem.transIconTo(newIcon: Drawable, duration: Int) {

        val crossfader = TransitionDrawable(arrayOf(this.icon, newIcon))
        this.icon = crossfader
        crossfader.startTransition(duration)
    }

    // Helper method to manage lock state
    private fun MenuItem.transStateTo(newState: LOCK, duration: Int){
        if (this.itemId != R.id.action_lock)
            return

        lateinit var trans: TransitionDrawable

        when(newState){
            LOCK.INVISIBLE -> {
                trans = TransitionDrawable(arrayOf(this.icon, ColorDrawable(Color.TRANSPARENT)))
                Handler().postDelayed({
                    runOnUiThread {
                        this.isVisible = false
                    }
                }, DRAWABLE_GRAD_TRANS_DURATION.toLong())
            }
            LOCK.CLOSE -> {
                this.isVisible = true
                trans = TransitionDrawable(arrayOf(this.icon, ContextCompat.getDrawable(baseContext, R.drawable.ic_action_lock_grad_2)))
            }
            LOCK.OPEN -> {
                this.isVisible = true
                trans = TransitionDrawable(arrayOf(this.icon, ContextCompat.getDrawable(baseContext, R.drawable.ic_action_lock_open_grad_2)))
            }
        }

        trans.isCrossFadeEnabled = true
        this.icon = trans
        trans.startTransition(duration)
    }

    // Inflate mToolbar menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        mToolbar.inflateMenu(R.menu.toolbar)
        return true
    }

    // Manage mToolbar actions
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

    // Inflate context (more) menu
    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.more, menu)
    }

    // Manage context (more) menu actions
    override fun onContextItemSelected(item: MenuItem?): Boolean {
        return when(item?.itemId){
            R.id.action_more_settings -> {
                Log.d("lol", "lol")
                true
            }
            else -> {
                super.onContextItemSelected(item)
            }
        }
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        with(navigation.menu){
            findItem(R.id.navigation_launchpad).setIcon(R.drawable.ic_section_home)
            findItem(R.id.navigation_shortcut).setIcon(R.drawable.ic_section_shortcut)
            findItem(R.id.navigation_system).setIcon(R.drawable.ic_section_system)
        }

        // TODO remove mock
        val mock = GridMock(baseContext)

        when (item.itemId) {
            R.id.navigation_launchpad -> {
                // Active apps
                recyclerView?.transBackgroundTo(ContextCompat.getDrawable(baseContext, R.color.grad_1)!!, DRAWABLE_GRAD_TRANS_DURATION)

                // NavigationBottomView
                item.setIcon(R.drawable.ic_section_home_grad_1)

                // Toolbar
                mToolbar.setTitle(R.string.title_launchpad)
                mToolbar.menu.findItem(R.id.action_edit)?.transIconTo(ContextCompat.getDrawable(baseContext, R.drawable.ic_action_edit_grad_1)!!, DRAWABLE_GRAD_TRANS_DURATION)
                mToolbar.menu.findItem(R.id.action_lock)?.transStateTo(LOCK.INVISIBLE, DRAWABLE_GRAD_TRANS_DURATION)

                // Update PagedGrid
                mGridAdapter.pages = arrayOf(mock.apps(4, 5), mock.coordinates(4,4))

                // Set up slider
                // TODO move this code away

                val slider = DokeySlider(this)

                mGridAdapter.pages[0].forEachIndexed { _, _, element ->
                    element.setOnInflateViewListener { view ->
                        view.setOnClickListener {
                            slider.show()
                        }
                    }
                }
                mGridAdapter.notifyDataSetChanged()

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_shortcut -> {
                // Active apps
                recyclerView?.transBackgroundTo(ContextCompat.getDrawable(baseContext, R.color.grad_2)!!, DRAWABLE_GRAD_TRANS_DURATION)

                // NavigationBottomView
                item.setIcon(R.drawable.ic_section_shortcut_grad_2)

                // Toolbar
                // TODO mToolbar.setTitle(focusedApp.getTitle())
                mToolbar.menu.findItem(R.id.action_edit)?.transIconTo(ContextCompat.getDrawable(baseContext, R.drawable.ic_action_edit_grad_2)!!, DRAWABLE_GRAD_TRANS_DURATION)
                mToolbar.menu.findItem(R.id.action_lock)?.transStateTo(lockState, DRAWABLE_GRAD_TRANS_DURATION)


                // Update PagedGrid
                mGridAdapter.pages = arrayOf(mock.apps(4, 5), mock.coordinates(4,4))
                mGridAdapter.notifyDataSetChanged()

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_system -> {
                // Active apps
                recyclerView?.transBackgroundTo(ContextCompat.getDrawable(baseContext, R.color.grad_3)!!, DRAWABLE_GRAD_TRANS_DURATION)

                // NavigationBottomView
                item.setIcon(R.drawable.ic_section_system_grad_3)

                // Toolbar
                mToolbar.setTitle(R.string.title_system)
                mToolbar.menu.findItem(R.id.action_edit)?.transIconTo(ContextCompat.getDrawable(baseContext, R.drawable.ic_action_edit_grad_3)!!, DRAWABLE_GRAD_TRANS_DURATION)
                mToolbar.menu.findItem(R.id.action_lock)?.transStateTo(LOCK.INVISIBLE, DRAWABLE_GRAD_TRANS_DURATION)

                // Update PagedGrid
                mGridAdapter.pages = arrayOf(mock.apps(4, 5), mock.coordinates(4,4))
                mGridAdapter.notifyDataSetChanged()

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_more -> {
                // Setup more menu
                val popupMenu = PopupMenu(this, findViewById(R.id.navigation_more))
                popupMenu.inflate(R.menu.more)
                popupMenu.setOnMenuItemClickListener(this)
                popupMenu.show()
                return@OnNavigationItemSelectedListener false
            }
        }
        false
    }

    // Manage more menu selection
    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return when(item?.itemId){
            R.id.action_more_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_more_docs -> {
                val intent = Intent(this@HomeActivity, WebActivity::class.java)
                intent.putExtra(WebActivity.INTENT_URL_KEY, getString(R.string.url_docs))
                startActivity(intent)
                true
            }
            else -> false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        mToolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(mToolbar)
        mToolbar.setTitle(R.string.title_launchpad)

        // Init RecyclerView for active apps
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = mActiveAppAdapter
        mActiveAppAdapter.activeApps = ActiveAppMock.Factory.list(baseContext, 9)
        mActiveAppAdapter.notifyDataSetChanged()

        // Init BottomNavigationView
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        navigation.itemIconTintList = null
        navigation.selectedItemId = R.id.navigation_launchpad // fire section selected event

        // Init PagedGridView
        pagedGridView.adapter = mGridAdapter
    }
}

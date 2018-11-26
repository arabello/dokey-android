package io.rocketguys.dokey

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.content.ContextCompat
import android.support.v7.preference.PreferenceManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import io.matteopellegrino.pagedgrid.adapter.GridAdapter
import io.rocketguys.dokey.connect.ConnectActivity
import io.rocketguys.dokey.connect.ScanActivity
import io.rocketguys.dokey.connect.statusbar.ContextualStatusbar
import io.rocketguys.dokey.connect.statusbar.Statusbar
import io.rocketguys.dokey.network.PENDING_INTENT_DISCONNECT_SERVICE
import io.rocketguys.dokey.network.activity.ConnectedActivity
import io.rocketguys.dokey.network.model.App
import io.rocketguys.dokey.padlock.MenuItemPadlock
import io.rocketguys.dokey.padlock.Padlock
import io.rocketguys.dokey.padlock.TransitionDrawablePadlock
import io.rocketguys.dokey.preferences.SettingsActivity
import io.rocketguys.dokey.sync.*
import kotlinx.android.synthetic.main.activity_home.*
import model.command.Command
import model.section.Section
import net.model.DeviceInfo
import java.util.*


class HomeActivity : ConnectedActivity(){
    companion object {
        private val TAG: String = HomeActivity::class.java.simpleName
        private const val TRANS_DRAWABLE_DURATION = 360
        const val ACTIVE_APPS_PULL_PERIOD = 2000L
    }

    // View
    private lateinit var mActiveAppAdapter: ActiveAppAdapter
    private lateinit var mToolbar: Toolbar
    private lateinit var connectingStatusbar: Statusbar
    private val mGridAdapter = GridAdapter(arrayOf())
    private var activeAppsTimer: Timer ?= null

    // State
    private lateinit var padlock: MenuItemPadlock
    private var disconnectFromActivity: Boolean = false

    // State - Section
    private var sectionAdapter: SectionAdapter? = null
    private var currentSectionId: String = SectionAdapter.LAUNCHPAD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        mToolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(mToolbar)
        mToolbar.setTitle(R.string.title_launchpad)

        // Keep screen on pref set up
        val keepScreenOn = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_ux_keep_screen_on_key), true)
        Log.d(TAG, "keep screen on: $keepScreenOn")
        if(keepScreenOn)
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Init RecyclerView for active apps
        mActiveAppAdapter = ActiveAppAdapter(this, arrayListOf())
        val orientation = if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) LinearLayoutManager.HORIZONTAL else LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = LinearLayoutManager(this, orientation, false)
        recyclerView.adapter = mActiveAppAdapter

        // Init BottomNavigationView
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        navigation.itemIconTintList = null

        // Init PagedGridView
        pagedGridView.adapter = mGridAdapter

        // Init padlock
        padlock = TransitionDrawablePadlock(Padlock.OPEN, TRANS_DRAWABLE_DURATION)

        // Analyze the current intent to determine if a pending intent was passed from the notification
        setupFlagsForNotificationIntent(intent)

        // Set up interrupted status bar
        connectingStatusbar = ContextualStatusbar(this).connecting(rootView)
        connectingStatusbar.onShow = { overlay.visibility = View.VISIBLE }
        connectingStatusbar.onDismiss = { overlay.visibility = View.GONE }
    }

    // Inflate mToolbar menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        mToolbar.inflateMenu(R.menu.toolbar)
        padlock.menuItem = mToolbar.menu.findItem(R.id.action_padlock)
        return true
    }

    // Manage mToolbar actions
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_edit -> {
            networkManagerService?.requestEditor(currentSectionId)
            true
        }

        R.id.action_padlock -> {
            padlock.toggle()
            if (padlock.`is`(Padlock.OPEN)) {

                val sectionId: String? = when (navigation.selectedItemId) {
                    R.id.navigation_launchpad -> SectionAdapter.LAUNCHPAD
                    R.id.navigation_shortcut -> SectionAdapter.SHORTCUT
                    R.id.navigation_system -> SectionAdapter.SYSTEM
                    else -> null
                }
                if (sectionId != null)
                    networkManagerService?.requestSection(sectionId) { section, associatedApp ->
                        Log.d(TAG, "requestSection ${section?.name}")
                        if (associatedApp != null) mToolbar.title = associatedApp.name
                        renderSectionWithFallback(section?.id, section, associatedApp)
                    }
            }
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }


    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        if (item.itemId != R.id.navigation_more)
            with(navigation.menu){
                findItem(R.id.navigation_launchpad).setIcon(R.drawable.ic_section_home)
                findItem(R.id.navigation_shortcut).setIcon(R.drawable.ic_section_shortcut)
                findItem(R.id.navigation_system).setIcon(R.drawable.ic_section_system)
            }



        when (item.itemId) {
            R.id.navigation_launchpad -> {
                // Active apps
                recyclerView?.transBackgroundTo(ContextCompat.getDrawable(baseContext, R.color.grad_1)!!, TRANS_DRAWABLE_DURATION)

                // NavigationBottomView
                item.setIcon(R.drawable.ic_section_home_grad_1)

                // Toolbar
                mToolbar.setTitle(R.string.title_launchpad)
                mToolbar.menu.findItem(R.id.action_edit)?.transIconTo(ContextCompat.getDrawable(baseContext, R.drawable.ic_action_edit_grad_1)!!, TRANS_DRAWABLE_DURATION)

                padlock.icons[Padlock.CLOSE] = ContextCompat.getDrawable(baseContext, R.drawable.ic_action_lock_grad_1)!!
                padlock.icons[Padlock.OPEN] = ContextCompat.getDrawable(baseContext, R.drawable.ic_action_lock_open_grad_1)!!
                padlock.state = Padlock.OPEN

                // Update PagedGrid
                // Request the section
                networkManagerService?.requestSection(SectionAdapter.LAUNCHPAD){ section, _ ->
                    Log.d(TAG, "requestSection ${section?.name}")
                    renderSectionWithFallback(SectionAdapter.LAUNCHPAD, section)
                }

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_shortcut -> {
                // Active apps
                recyclerView?.transBackgroundTo(ContextCompat.getDrawable(baseContext, R.color.grad_2)!!, TRANS_DRAWABLE_DURATION)

                // NavigationBottomView
                item.setIcon(R.drawable.ic_section_shortcut_grad_2)

                // Toolbar
                //mToolbar.setTitle(R.string.title_shortcut)
                mToolbar.menu.findItem(R.id.action_edit)?.transIconTo(ContextCompat.getDrawable(baseContext, R.drawable.ic_action_edit_grad_2)!!, TRANS_DRAWABLE_DURATION)
                padlock.icons[Padlock.CLOSE] = ContextCompat.getDrawable(baseContext, R.drawable.ic_action_lock_grad_2)!!
                padlock.icons[Padlock.OPEN] = ContextCompat.getDrawable(baseContext, R.drawable.ic_action_lock_open_grad_2)!!
                padlock.state = Padlock.OPEN

                // Update PagedGrid
                // Request the section
                networkManagerService?.requestSection(SectionAdapter.SHORTCUT) { section, associatedApp ->
                    Log.d(TAG, "requestSection ${section?.name}")
                    mToolbar.title = associatedApp?.name
                    renderSectionWithFallback(section?.id, section, associatedApp)
                }


                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_system -> {
                // Active apps
                recyclerView?.transBackgroundTo(ContextCompat.getDrawable(baseContext, R.color.grad_3)!!, TRANS_DRAWABLE_DURATION)

                // NavigationBottomView
                item.setIcon(R.drawable.ic_section_system_grad_3)

                // Toolbar
                mToolbar.setTitle(R.string.title_system)
                mToolbar.menu.findItem(R.id.action_edit)?.transIconTo(ContextCompat.getDrawable(baseContext, R.drawable.ic_action_edit_grad_3)!!, TRANS_DRAWABLE_DURATION)
                padlock.icons[Padlock.CLOSE] = ContextCompat.getDrawable(baseContext, R.drawable.ic_action_lock_grad_3)!!
                padlock.icons[Padlock.OPEN] = ContextCompat.getDrawable(baseContext, R.drawable.ic_action_lock_open_grad_3)!!
                padlock.state = Padlock.OPEN

                // Update PagedGrid
                // Request the section
                networkManagerService?.requestSection(SectionAdapter.SYSTEM){ section, _ ->
                    Log.d(TAG, "requestSection ${section?.name}")
                    renderSectionWithFallback(SectionAdapter.SYSTEM, section)
                }

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_more -> {
                // Setup more menu

                val popupMenu = NoFocusPopupMenu.Builder(this)
                        .setAnchorView(findViewById(R.id.navigation_more))
                        .addItem(getString(R.string.title_more_settings)){
                            startActivityForResult(Intent(this, SettingsActivity::class.java), SettingsActivity.REQUEST_CODE)
                        }
                        .addItem(getString(R.string.title_more_docs)){
                            val intent = Intent(this@HomeActivity, WebActivity::class.java)
                            intent.putExtra(WebActivity.INTENT_URL_KEY, getString(R.string.url_docs))
                            startActivity(intent)
                        }
                        .addItem(getString(R.string.title_more_disconnect)){
                            disconnectFromActivity = true
                            showDisconnectConfirmationDialog()
                        }
                        .create()

                popupMenu.show()
                return@OnNavigationItemSelectedListener false
            }
        }
        false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode){
            SettingsActivity.REQUEST_CODE -> {
                if(resultCode == RESULT_OK){
                    // A preference in SettingsActivity changed and activity should be refreshed
                    recreate()
                }
            }
        }
    }

    private fun renderSectionWithFallback(sectionId: String?, section: Section?, application: App ?= null){
        if (section == null || section.isEmpty()){
            // Section does not exist, show no section layout fallback
            noSectionFallback.visibility = View.VISIBLE
            pagedGridView.visibility = View.GONE

            when(sectionId){
                SectionAdapter.LAUNCHPAD -> {
                    currentSectionId = SectionAdapter.LAUNCHPAD
                    noSectionText.text = getString(R.string.acty_home_no_section_msg, getString(R.string.title_launchpad))
                    noSectionBtn.background = ContextCompat.getDrawable(this@HomeActivity, R.drawable.btn_bg_grad_1)
                    noSectionBtn.setOnClickListener { networkManagerService?.requestEditor(SectionAdapter.LAUNCHPAD) }
                }

                SectionAdapter.SYSTEM -> {
                    currentSectionId = SectionAdapter.SYSTEM
                    noSectionText.text = getString(R.string.acty_home_no_section_msg, getString(R.string.title_system))
                    noSectionBtn.background = ContextCompat.getDrawable(this@HomeActivity, R.drawable.btn_bg_grad_3)
                    noSectionBtn.setOnClickListener { networkManagerService?.requestEditor(SectionAdapter.SYSTEM) }
                }

                // Shortcut/App
                else -> {
                    currentSectionId = application?.relatedSectionId() ?: "" // Avoid crash: Counter effect. a modified section may not be rendered (extremely rare case)
                    mToolbar.title = application?.name ?: ""
                    noSectionText.text = getString(R.string.acty_home_no_section_msg, application?.name ?: "")
                    noSectionBtn.background = ContextCompat.getDrawable(this@HomeActivity, R.drawable.btn_bg_grad_2)
                    noSectionBtn.setOnClickListener { application?.requestInEditor() }
                }
            }

        }else{
            currentSectionId = section.id!!
            // Section exists, render it
            noSectionFallback.visibility = View.GONE
            pagedGridView.visibility = View.VISIBLE
            sectionAdapter?.notifySectionChanged(section)
            pagedGridView.currentPage = 0 // reset page selection
        }
    }

    // Shortcut section related
    override fun onApplicationSwitch(application: App, section: Section?) {
        Log.d(TAG, "onApplicationSwitch $application.name ${section?.name}")

        // Update only if current navigation is shortcut section and the lock is open
        if (navigation.selectedItemId == R.id.navigation_shortcut && padlock.`is`(Padlock.OPEN)) {
            mToolbar.title = application.name
            renderSectionWithFallback(section?.id, section, application)
        }
    }

    // Used to avoid multiple evaluations of flags
    private var notificationFlagEvaluated = false

    // Variables used in the communication between the notification and the activity
    private var notificationDisconnectRequestFlag = false

    /**
     * This method should be called when a new intent is expected, as in the onCreate or
     * onNewIntent method.
     * Analyze the given intent to determine which notification flags to set and which not.
     */
    private fun setupFlagsForNotificationIntent(intent: Intent?) {
        // Reset all the notification-related variables
        notificationDisconnectRequestFlag = false

        if (intent != null) {
            if (intent.hasExtra(PENDING_INTENT_DISCONNECT_SERVICE)) {  // Disconnect request sent
                notificationDisconnectRequestFlag = true
            }
        }

        notificationFlagEvaluated = false
    }

    /**
     * This method evaluates the notification flags set by the "setupFlagsForNotificationIntent"
     * method and should be called in a context where the "networkManagerService" is already bounded.
     */
    private fun evaluateNotificationFlags() {
        // Filter out requests without a binded service
        if (networkManagerService == null) {
            return
        }

        // Filter out multiple request evaluations
        if (notificationFlagEvaluated) {
            return
        }

        if (notificationDisconnectRequestFlag) {
            showDisconnectConfirmationDialog()
        }

        notificationFlagEvaluated = true
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent

        // New intent has been received, parse the notification flags and evaluate them
        setupFlagsForNotificationIntent(intent)
        evaluateNotificationFlags()
    }

    override fun onResume() {
        super.onResume()

        navigation.selectedItemId = R.id.navigation_launchpad // fire section selected event

    }

    override fun onStop() {
        super.onStop()
        activeAppsTimer?.cancel()
        activeAppsTimer = null
    }

    override fun onSectionModified(section: Section, associatedApp: App?) {
        Log.d(TAG, "onSectionModified: current: $currentSectionId")
        Log.d(TAG, "onSectionModified: modified: ${section.id}")
        if (section.id == currentSectionId)
            renderSectionWithFallback(section.id!!, section, associatedApp)
    }

    // Command may not be in the section.
    // Check if exists in the current section and update
    override fun onCommandModified(command: Command) {
        Log.d(TAG, "onCommandModified ${command.title}")
    }

    // Android lifecycle : called after onStart()
    override fun onServiceConnected() {
        Log.d(TAG, ":onServiceConnected")

        // Evaluate the current notification flags
        evaluateNotificationFlags()

        // Restart ActiveApp pull
        activeAppsTimer = Timer()
        activeAppsTimer?.schedule(
                ActiveAppTask(networkManagerService, mActiveAppAdapter),
                0L, ACTIVE_APPS_PULL_PERIOD)

        sectionAdapter = SectionConnectedAdapter(mGridAdapter, this, networkManagerService)

        // Request the section
        networkManagerService?.requestSection(SectionAdapter.LAUNCHPAD){ section, _ ->
            Log.d(TAG, "requestSection ${section?.name}")
            renderSectionWithFallback(SectionAdapter.LAUNCHPAD, section)
        }

        // Request the section
        networkManagerService?.requestSection(SectionAdapter.SHORTCUT){ section, _ ->
            Log.d(TAG, "requestSection ${section?.name}")
        }

        // Request the section
        networkManagerService?.requestSection(SectionAdapter.SYSTEM){ section, _ ->
            Log.d(TAG, "requestSection ${section?.name}")
        }

        if (networkManagerService != null && networkManagerService?.isReconnecting!!)
            connectingStatusbar.show()
    }



    override fun onConnectionInterrupted() {
        Log.d(TAG, ":onConnectionInterrupted isReconnecting? ${networkManagerService?.isReconnecting}. isConnected? ${networkManagerService?.isConnected}")

        // Disable UI
        connectingStatusbar.show()
    }

    override fun onConnectionReestablished(serverInfo: DeviceInfo) {
        Log.d(TAG, ":onConnectionReestablished isReconnecting? ${networkManagerService?.isReconnecting}. isConnected? ${networkManagerService?.isConnected}")

        // Enable UI
        connectingStatusbar.dismiss()
    }

    override fun onConnectionClosed() {
        Log.d(TAG, "onConnectionClosed")

        when {
            // Disconnect from notification request
            notificationDisconnectRequestFlag -> finish()

            // Disconnect from activity, user want to change desktop. Clear cache
            disconnectFromActivity -> {

                val intent = Intent(this, ConnectActivity::class.java)
                when(ConnectActivity.LAST_CONNECTION_TYPE){
                    ConnectActivity.QR_CODE -> ScanActivity.cache(this).clear()
                    ConnectActivity.USB -> intent.putExtra(ConnectActivity.EXTRA_DISABLE_USB_DAEMON, true)
                }

                startActivity(intent)
                finish()
            }

            // Everything else, connection lost
            else -> {
                startActivity(Intent(this, ConnectActivity::class.java))
                finish()
            }
        }
    }

    private fun showDisconnectConfirmationDialog() {
        AlertDialog.Builder(this)
                .setTitle(getString(R.string.dlg_disconnect_title))
                .setMessage(getString(R.string.dlg_disconnect_msg))
                .setPositiveButton(getString(R.string.dlg_disconnect_positive)) { _, _ ->
                    // Stop service
                    stopNetworkService()
                }
                .setNegativeButton(getString(R.string.dlg_disconnect_negative), null).show()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus and PreferenceManager.getDefaultSharedPreferences(this)
                        .getBoolean(getString(R.string.pref_ux_fullscreen_key), false)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            }else{
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN)
            }
        }
    }

    fun isPadlockOpen(): Boolean = padlock.`is`(Padlock.OPEN)
}

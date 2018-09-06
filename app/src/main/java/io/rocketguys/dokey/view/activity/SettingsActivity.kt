package io.rocketguys.dokey.view.activity

import android.content.Intent
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.PreferenceScreen
import android.support.v4.app.NavUtils
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import io.rocketguys.dokey.R
import java.io.File

class SettingsActivity : AppCompatPreferenceActivity() {
    lateinit var mToolbar: Toolbar

    class SettingsFragment : PreferenceFragment() {

        private fun deleteCache() {
            deleteDir(activity.applicationContext.cacheDir)
        }

        private fun deleteDir(dir: File?) {
            if (dir != null) {
                if (dir.isFile){
                    dir.delete()
                }else{
                    for (f in dir.list())
                        deleteDir(File(dir, f))
                }
            }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(io.rocketguys.dokey.R.xml.preferences)

        }

        override fun onPreferenceTreeClick(preferenceScreen: PreferenceScreen, preference: Preference): Boolean {

            when(preference.key){
                getString(R.string.pref_web_website_key) -> {
                    val intent = Intent(activity, WebActivity::class.java)
                    intent.putExtra(WebActivity.INTENT_URL_KEY, getString(R.string.url_dokey_io))
                    startActivity(intent)
                    activity.finish()
                }

                getString(R.string.pref_web_credits_key) -> {
                    val intent = Intent(activity, WebActivity::class.java)
                    intent.putExtra(WebActivity.INTENT_URL_KEY, getString(R.string.url_credits))
                    startActivity(intent)
                    activity.finish()
                }

                getString(R.string.pref_advanced_cache_key) -> {
                    AlertDialog.Builder(activity)
                            .setMessage(activity.getString(R.string.dlg_clear_cache_msg))
                            .setNegativeButton(activity.getString(R.string.dlg_clear_cache_cancel)){ _, _ -> }
                            .setPositiveButton(activity.getString(R.string.dlg_clear_cache_ok)){ _, _ ->
                                // Clear cache
                                deleteCache()
                            }.create().show()
                }
            }

            return true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        mToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(mToolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        fragmentManager.beginTransaction().replace(R.id.preferences_container, SettingsFragment()).commit()
    }

    override fun onMenuItemSelected(featureId: Int, item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this)
            }
            return true
        }
        return super.onMenuItemSelected(featureId, item)
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    override fun isValidFragment(fragmentName: String): Boolean {
        return PreferenceFragment::class.java.name == fragmentName
    }
}


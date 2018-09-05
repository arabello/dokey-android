package io.rocketguys.dokey.view

import android.content.Intent
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.PreferenceScreen
import android.support.v4.app.NavUtils
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import io.rocketguys.dokey.R
import java.util.HashMap

class SettingsActivity : AppCompatPreferenceActivity() {
    lateinit var mToolbar: Toolbar

    class SettingsFragment : PreferenceFragment() {

        val webResMap = mutableMapOf<String, String>()

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(io.rocketguys.dokey.R.xml.preferences)

            webResMap[getString(R.string.pref_web_website_key)] = getString(R.string.url_dokey_io)
            webResMap[getString(R.string.pref_web_credits_key)] =getString(R.string.url_credits)
        }

        override fun onPreferenceTreeClick(preferenceScreen: PreferenceScreen, preference: Preference): Boolean {
            val key = preference.key

            if (webResMap.containsKey(key)){
                val intent = Intent(activity, WebActivity::class.java)
                intent.putExtra(WebActivity.INTENT_URL_KEY, webResMap[key])
                startActivity(intent)
                activity.finish()
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


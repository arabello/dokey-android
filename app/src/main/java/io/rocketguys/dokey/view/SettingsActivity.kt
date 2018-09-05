package io.rocketguys.dokey.view

import android.os.Bundle
import android.preference.PreferenceFragment
import android.support.v4.app.NavUtils
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import io.rocketguys.dokey.R

class SettingsActivity : AppCompatPreferenceActivity() {
    lateinit var mToolbar: Toolbar

    class SettingsFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(io.rocketguys.dokey.R.xml.preferences)
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


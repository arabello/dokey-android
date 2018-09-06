package io.rocketguys.dokey.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import io.rocketguys.dokey.network.NetworkEvent
import io.rocketguys.dokey.network.activity.ConnectedActivity
import io.rocketguys.dokey.network.activity.ConnectionBuilderActivity
import io.rocketguys.dokey.network.activity.NetworkActivity
import model.command.Command
import model.section.Section
import net.model.DeviceInfo

class TestNavigationDrawerActivity : ConnectedActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("CREATED", "Test navigation")

        window.decorView.setBackgroundColor(Color.GREEN)
    }

    override fun onStop() {
        super.onStop()

        // Close the current connection
        networkManagerService?.closeConnection()
    }

    override fun onServiceConnected() {
        // Request the section
        networkManagerService?.requestSection("launchpad") {
            Log.d("SECTION", it?.json().toString())

            it?.pages?.forEach { page ->
                page.components?.forEach { component ->
                    // Request each command
                    networkManagerService?.requestCommand(component.commandId!!) {
                        Log.d("COMMAND", it?.json().toString())

                        // Request the image
                        networkManagerService?.requestImage(it?.iconId!!) { imageId, imageFile ->
                            Log.d("IMAGE", imageFile?.absolutePath)
                        }
                    }
                }
            }
        }
    }

    override fun onSectionModified(section: Section) {
        Log.d("SEC_MODIFIED", section.json().toString())
    }

    override fun onCommandModified(command: Command) {
        Log.d("COMM_MODIFIED", command.json().toString())
    }

    override fun onApplicationSwitch(applicationName: String, section: Section?) {
        Log.d("SWITCH", applicationName)
        Log.d("SWITCH", section?.json().toString())
    }

    override fun onConnectionClosed() {
        Log.d("CONNECT", "Connection closed")
    }
}
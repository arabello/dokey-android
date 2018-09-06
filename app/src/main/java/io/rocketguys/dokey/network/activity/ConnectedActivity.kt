package io.rocketguys.dokey.network.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.rocketguys.dokey.network.NetworkEvent
import io.rocketguys.dokey.network.util.DeviceInfoBuilder.deviceInfo
import json.JSONObject
import model.command.Command
import model.parser.command.TypeCommandParser
import model.parser.component.CachingComponentParser
import model.parser.page.DefaultPageParser
import model.parser.section.DefaultSectionParser
import model.section.Section
import net.model.DeviceInfo

abstract class ConnectedActivity : NetworkActivity() {
    /**
     * Called when a Section is modified in the Desktop editor
     */
    abstract fun onSectionModified(section: Section)

    /**
     * Called when a Command is modified in the Desktop app
     */
    abstract fun onCommandModified(command: Command)

    /**
     * Called when the user switches to another application
     */
    abstract fun onApplicationSwitch(applicationName: String, section: Section?)

    /**
     * Called when the connection with the desktop server is interrupted.
     */
    abstract fun onConnectionClosed()

    override fun onStart() {
        super.onStart()

        // Register the broadcast listeners
        broadcastManager?.registerReceiver(NetworkEvent.SECTION_MODIFIED_EVENT, sectionModifiedReceiver)
        broadcastManager?.registerReceiver(NetworkEvent.COMMAND_MODIFIED_EVENT, commandModifiedReceiver)
        broadcastManager?.registerReceiver(NetworkEvent.APPLICATION_SWITCH_EVENT, applicationSwitchReceiver)
        broadcastManager?.registerReceiver(NetworkEvent.CONNECTION_CLOSED_EVENT, connectionClosedReceiver)
    }

    override fun onStop() {
        super.onStop()

        // Unregister all the listeners
        broadcastManager?.unregisterReceiver(sectionModifiedReceiver)
        broadcastManager?.unregisterReceiver(commandModifiedReceiver)
        broadcastManager?.unregisterReceiver(applicationSwitchReceiver)
        broadcastManager?.unregisterReceiver(connectionClosedReceiver)
    }

    private val sectionModifiedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val sectionId = intent?.getStringExtra("payload")

            networkManagerService?.requestSection(sectionId!!, forceCache = true) { section ->
                if (section != null) {
                    onSectionModified(section)
                }
            }
        }
    }

    private val commandModifiedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val commandId = intent?.getStringExtra("payload")?.toInt()

            networkManagerService?.requestCommand(commandId!!, forceCache = true) {command ->
                if (command != null) {
                    onCommandModified(command)
                }
            }
        }
    }

    private val applicationSwitchReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val payload = intent?.getStringExtra("payload")
            val payloadJson = JSONObject(payload)

            val applicationName = payloadJson.getString("appName")
            val sectionId = payloadJson.optString("sectionId", null)
            val lastEdit = payloadJson.optLong("lastEdit", -1)

            if (sectionId == null) {  // No section is available for the current app
                onApplicationSwitch(applicationName, null)
            }else{  // Section is available, request it from the service.
                networkManagerService?.requestSection(sectionId, remoteLastEdit = lastEdit) {
                    section -> onApplicationSwitch(applicationName, section)
                }
            }
        }
    }

    private val connectionClosedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            onConnectionClosed()
        }
    }
}
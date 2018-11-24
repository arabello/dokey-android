package io.rocketguys.dokey.network.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.rocketguys.dokey.network.NetworkEvent
import io.rocketguys.dokey.network.model.App
import json.JSONObject
import model.command.Command
import model.section.Section
import net.model.DeviceInfo

abstract class ConnectedActivity : NetworkActivity() {
    /**
     * Called when a Section is modified in the Desktop editor
     */
    abstract fun onSectionModified(section: Section, associatedApp: App?)

    /**
     * Called when a Command is modified in the Desktop app
     */
    abstract fun onCommandModified(command: Command)

    /**
     * Called when the user switches to another application
     */
    abstract fun onApplicationSwitch(application: App, section: Section?)

    /**
     * Called when the connection is interrupted, and the service started the recovering
     * process.
     * If the service could recover it correctly, the onConnectionEstabled() callback
     * will be invoked. If the connection could not be recovered, onConnectionClosed()
     * will be called.
     */
    abstract fun onConnectionInterrupted()

    /**
     * Called when a connection with the dokey server has been reestablished, after being
     * interrupted.
     */
    abstract fun onConnectionReestablished(serverInfo: DeviceInfo)

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
        broadcastManager?.registerReceiver(NetworkEvent.CONNECTION_INTERRUPTED_EVENT, connectionInterruptedReceiver)
        broadcastManager?.registerReceiver(NetworkEvent.CONNECTION_ESTABLISHED_EVENT, connectionReestablishedReceiver)
    }

    override fun onStop() {
        super.onStop()

        // Unregister all the listeners
        broadcastManager?.unregisterReceiver(sectionModifiedReceiver)
        broadcastManager?.unregisterReceiver(commandModifiedReceiver)
        broadcastManager?.unregisterReceiver(applicationSwitchReceiver)
        broadcastManager?.unregisterReceiver(connectionClosedReceiver)
        broadcastManager?.unregisterReceiver(connectionInterruptedReceiver)
        broadcastManager?.unregisterReceiver(connectionReestablishedReceiver)
    }

    private val sectionModifiedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val sectionId = intent?.getStringExtra("payload")

            networkManagerService?.requestSection(sectionId!!, forceCache = true) { section, associatedApp ->
                if (section != null) {
                    onSectionModified(section, associatedApp)
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
            val applicationPath = payloadJson.getString("path")
            val application = App(networkManagerService!!, applicationName, applicationPath)
            val sectionId = payloadJson.optString("sectionId", null)
            val lastEdit = payloadJson.optLong("lastEdit", -1)

            if (sectionId == null) {  // No section is available for the current app
                onApplicationSwitch(application, null)
            }else{  // Section is available, request it from the service.
                networkManagerService?.requestSection(sectionId, remoteLastEdit = lastEdit) {
                    section, _ -> onApplicationSwitch(application, section)
                }
            }
        }
    }

    private val connectionClosedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            onConnectionClosed()
        }
    }

    private val connectionInterruptedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            onConnectionInterrupted()
        }
    }

    private val connectionReestablishedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val deviceInfoPayload = intent?.getStringExtra("payload")
            val deviceInfo = DeviceInfo.fromJson(JSONObject(deviceInfoPayload))
            onConnectionReestablished(deviceInfo)
        }
    }
}
package io.rocketguys.dokey.network.model

import io.rocketguys.dokey.network.NetworkManagerService
import java.io.File

/**
 * This class represents an application and it is used in the active app request.
 */
class App(val networkManagerService: NetworkManagerService, val name: String, val path: String) {
    /**
     * Request the icon of the current image, using the "requestImage" method of
     * the NetworkManagerService
     */
    fun requestIcon(callback: (imageId: String, imageFile: File?) -> Unit) {
        networkManagerService.requestImage("app:$path", callback)
    }

    /**
     * Request focus to the current app.
     */
    fun requestFocus() {
        networkManagerService.requestAppFocus(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as App

        if (name != other.name) return false
        if (path != other.path) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + path.hashCode()
        return result
    }

    override fun toString(): String {
        return "App(name='$name', path='$path')"
    }
}
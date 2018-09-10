package io.rocketguys.dokey.network.cache
import android.content.Context
import io.rocketguys.dokey.network.util.NetworkUtils
import java.io.File

abstract class AbstractCache<K, V>(val context: Context, serverIdentifier : String,
                                   cacheFolderName: String) {

    // This map will hold all loaded items in memory
    protected val cacheMap = mutableMapOf<K, V>()

    // Calculate the md5 hash of the computer identifier, used to find file paths
    protected val serverHash : String = NetworkUtils.md5(serverIdentifier)

    protected val currentServerDir : File

    protected val itemsCacheDir : File

    init {
        // Initialize the directories
        currentServerDir = File(context.cacheDir, serverHash)
        if (!currentServerDir.isDirectory) {
            currentServerDir.mkdir()
        }

        itemsCacheDir = File(currentServerDir, cacheFolderName)
        if (!itemsCacheDir.isDirectory) {
            itemsCacheDir.mkdir()
        }
    }

    protected fun getItem(id: K) : V? {
        if (cacheMap.containsKey(id)) {  // Check in the in-memory cache
            return cacheMap[id]
        }else{  // Not present in memory, try to load it from files
            val item = loadItemFromCacheDir(id)
            if (item != null) {
                cacheMap[id] = item
                return item
            }
        }

        // Command not found in cache
        return null
    }

    abstract fun loadItemFromCacheDir(id: K) : V?
}
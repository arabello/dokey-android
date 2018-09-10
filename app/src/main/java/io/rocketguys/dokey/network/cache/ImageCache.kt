package io.rocketguys.dokey.network.cache

import android.content.Context
import io.rocketguys.dokey.network.util.NetworkUtils
import java.io.File

class ImageCache(context: Context, serverIdentifier : String)
    : AbstractCache<String, File>(context, serverIdentifier, "images"){

    private val imageCacheDir : File = itemsCacheDir

    fun getImageFile(id: String) : File? {
        return getItem(id)
    }

    @Synchronized
    fun saveImage(id: String, image: File) : File {
        // Hash the image id to avoid problems in the name
        val imageHash = NetworkUtils.md5(id)
        val cacheImageFile = File(imageCacheDir, "$imageHash.png")

        // Copy the given image file to the cache
        image.copyTo(cacheImageFile, true)

        // Save the image in the memory-cache
        cacheMap[id] = cacheImageFile

        return cacheImageFile
    }

    override fun loadItemFromCacheDir(id: String): File? {
        // Hash the image id to avoid problems in the name
        val imageHash = NetworkUtils.md5(id)

        val imageFile = File(imageCacheDir, "$imageHash.png")
        if (imageFile.isFile) {
            return imageFile
        }

        // Image not found in cache
        return null
    }
}
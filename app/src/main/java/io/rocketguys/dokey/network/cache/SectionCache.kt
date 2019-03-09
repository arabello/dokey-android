package io.rocketguys.dokey.network.cache

import android.content.Context
import io.rocketguys.dokey.network.util.NetworkUtils
import json.JSONException
import json.JSONObject
import json.JSONTokener
import model.parser.section.SectionParser
import model.section.Section
import java.io.*

class SectionCache(context: Context, val sectionParser: SectionParser, serverIdentifier : String)
    : AbstractCache<String, Section>(context, serverIdentifier, "sections"){

    private val sectionCacheDir : File = itemsCacheDir

    fun getSection(id: String) : Section? {
        return getItem(id)
    }

    @Synchronized
    fun saveSection(section: Section) {
        // Hash the section id to avoid problems in the name
        val sectionHash = NetworkUtils.md5(section.id!!)

        val sectionFile = File(sectionCacheDir, "$sectionHash.json")
        try {
            // Write the json section to the file
            val fos = FileOutputStream(sectionFile)
            val pw = PrintWriter(fos)
            val sectionJson = section.json()
            pw.write(sectionJson.toString())
            pw.close()

            // Also save it to the memory cache
            cacheMap[section.id!!] = section
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun loadItemFromCacheDir(id: String): Section? {
        // Hash the section id to avoid problems in the name
        val sectionHash = NetworkUtils.md5(id)

        val sectionFile = File(sectionCacheDir, "$sectionHash.json")
        if (sectionFile.isFile) {
            try {
                val fis = FileInputStream(sectionFile)
                val tokener = JSONTokener(fis)
                val jsonContent = JSONObject(tokener)
                val section = sectionParser.fromJSON(jsonContent)
                fis.close()
                return section
            }catch (e: JSONException) {  // Cache not valid, invalidate
                sectionFile.delete()
            }
        }

        // Section not found in cache
        return null
    }
}
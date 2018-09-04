package io.rocketguys.dokey.network.cache

import android.content.Context
import io.rocketguys.dokey.network.util.NetworkUtils
import json.JSONObject
import json.JSONTokener
import model.command.Command
import model.parser.command.CommandParser
import java.io.*

class CommandCache(context: Context, val commandParser: CommandParser, serverIdentifier : String)
    : AbstractCache<Int, Command>(context, serverIdentifier, "commands"){

    private val commandCacheDir : File = itemsCacheDir

    fun getCommand(id: Int) : Command? {
        return getItem(id)
    }

    @Synchronized
    fun saveCommand(command: Command) {
        val commandFile = File(commandCacheDir, "${command.id}.json")
        try {
            // Write the json command to the file
            val fos = FileOutputStream(commandFile)
            val pw = PrintWriter(fos)
            val commandJson = command.json()
            pw.write(commandJson.toString())
            pw.close()

            // Also save it to the memory cache
            cacheMap[command.id!!] = command
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun loadItemFromCacheDir(id: Int): Command? {
        val commandFile = File(commandCacheDir, "$id.json")
        if (commandFile.isFile) {
            val fis = FileInputStream(commandFile)
            val tokener = JSONTokener(fis)
            val jsonContent = JSONObject(tokener)
            val command = commandParser.fromJSON(jsonContent)
            fis.close()
            return command
        }

        // Command not found in cache
        return null
    }
}
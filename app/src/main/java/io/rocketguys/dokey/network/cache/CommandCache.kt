package io.rocketguys.dokey.network.cache

import android.content.Context
import io.rocketguys.dokey.network.util.NetworkUtils
import json.JSONObject
import json.JSONTokener
import model.command.Command
import model.parser.command.CommandParser
import java.io.*

class CommandCache(val context: Context, val commandParser: CommandParser, serverIdentifier : String) {

    // This map will hold all loaded commands in memory
    private val cacheMap = mutableMapOf<Int, Command>()

    // Calculate the md5 hash of the computer identifier, used to find file paths
    private val serverHash : String = NetworkUtils.md5(serverIdentifier)

    private val commandCacheDir : File

    init {
        // Initialize the command cache dir
        commandCacheDir = File(context.cacheDir, serverHash)
        if (!commandCacheDir.isDirectory) {
            commandCacheDir.mkdir()
        }
    }

    fun getCommand(id: Int) : Command? {
        if (cacheMap.containsKey(id)) {  // Check in the in-memory cache
            return cacheMap[id]
        }else{  // Not present in memory, try to load it from files
            val command = loadCommandFromCache(id)
            if (command != null) {
                cacheMap[id] = command
                return command
            }
        }

        // Command not found in cache
        return null
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

    private fun loadCommandFromCache(id: Int): Command? {
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
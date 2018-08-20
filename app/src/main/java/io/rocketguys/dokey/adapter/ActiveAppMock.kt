package io.rocketguys.dokey.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileInputStream

/**
 * TODO: Add class description
 *
 * @author Matteo Pellegrino matteo.pelle.pellegrino@gmail.com
 */
interface ActiveAppMock{
    object Factory{
        fun list(context: Context, size: Int) = Array<ActiveAppMock>(size){
                val dir = File("${context.filesDir}${File.separator}mock")
                val pos = (Math.random() * dir.listFiles().size).toInt()
                object : ActiveAppMock {
                    override val bitmap = BitmapFactory.decodeStream(FileInputStream(dir.listFiles()[pos]))
                }
            }.toList()
    }
    val bitmap: Bitmap
}
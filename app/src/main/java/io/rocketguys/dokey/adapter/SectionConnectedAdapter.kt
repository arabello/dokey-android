package io.rocketguys.dokey.adapter

import android.graphics.BitmapFactory
import io.matteopellegrino.pagedgrid.adapter.GridAdapter
import io.matteopellegrino.pagedgrid.element.BitmapIcon
import io.matteopellegrino.pagedgrid.grid.EmptyGrid
import io.matteopellegrino.pagedgrid.grid.Grid
import io.rocketguys.dokey.network.NetworkManagerService
import io.rocketguys.dokey.network.activity.ConnectedActivity
import model.section.Section

/**
 * TODO: Add class description
 *
 * @author Matteo Pellegrino matteo.pelle.pellegrino@gmail.com
 */
class SectionConnectedAdapter(val gridAdapter: GridAdapter,
                              val activity: ConnectedActivity,
                              val networkManagerService: NetworkManagerService?) : SectionAdapter{

    override fun notifySectionChanged(section: Section?) {
        gridAdapter.pages = arrayOf<Grid>()

        section?.pages?.forEach { page ->
            val grid = EmptyGrid(page.colCount!!, page.rowCount!!)
            gridAdapter.pages += grid

            page.components?.forEach { component ->

                // Request each command
                networkManagerService?.requestCommand(component.commandId!!) { cmd ->
                    //Log.d("COMMAND", cmd?.json().toString())

                    // Request the image
                    networkManagerService?.requestImage(cmd?.iconId!!) { imageId, imageFile ->
                        //Log.d("IMAGE", imageFile?.absolutePath)

                        val bitmap = BitmapFactory.decodeFile(imageFile?.absolutePath)
                        grid[component.x!!, component.y!!] = BitmapIcon(cmd.title!!, bitmap)

                        activity.runOnUiThread {
                            gridAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
        }

        gridAdapter.notifyDataSetChanged()
    }
}

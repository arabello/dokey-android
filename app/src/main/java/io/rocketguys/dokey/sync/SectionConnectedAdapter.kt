package io.rocketguys.dokey.sync

import android.util.Log
import io.matteopellegrino.pagedgrid.adapter.GridAdapter
import io.matteopellegrino.pagedgrid.grid.EmptyGrid
import io.rocketguys.dokey.network.NetworkManagerService
import io.rocketguys.dokey.network.activity.ConnectedActivity
import model.section.Section
import kotlin.properties.Delegates

/**
 * This adapter implements the responsibility of [SectionAdapter].
 * It use a [GridAdapter] to render a given [Section].
 * The [NetworkManagerService] is used to request [Section] content
 * to the desktop server and to attach [NetworkManagerService.executeCommand]
 * to the [Section] content rendered.
 *
 * @author Matteo Pellegrino matteo.pelle.pellegrino@gmail.com
 */
class SectionConnectedAdapter(val gridAdapter: GridAdapter,
                              val activity: ConnectedActivity,
                              val networkManagerService: NetworkManagerService?) : SectionAdapter {
    companion object {
        private val TAG: String = SectionConnectedAdapter::class.java.simpleName
    }

    // This is called in UIThread
    override fun notifySectionChanged(section: Section?) {
        Log.d(TAG, "notifySectionChanged ${section?.name}")
        gridAdapter.pages = arrayOf()

        section?.pages?.forEach { page ->
            val grid = EmptyGrid(page.colCount!!, page.rowCount!!)
            gridAdapter.pages += grid

            page.components?.forEach { component ->
                grid[component.x!!, component.y!!] = CommandElement(component, networkManagerService, activity)
            }
        }

        gridAdapter.notifyDataSetChanged()
    }
}

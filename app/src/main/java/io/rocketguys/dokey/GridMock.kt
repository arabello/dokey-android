package io.rocketguys.dokey

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.matteopellegrino.pagedgrid.element.AbstractElement
import io.matteopellegrino.pagedgrid.element.BitmapIcon
import io.matteopellegrino.pagedgrid.grid.EmptyGrid
import io.matteopellegrino.pagedgrid.grid.Grid
import io.rocketguys.dokey.adapter.ActiveAppMock

/**
 * TODO: Add class description
 *
 * @author Matteo Pellegrino matteo.pelle.pellegrino@gmail.com
 */
class GridMock(val context: Context){
    fun coordinates(columns: Int, rows: Int): Grid{
        val grid = EmptyGrid(columns, rows)

        grid.forEachIndexed { x, y, _ ->
            grid[x, y] = object : AbstractElement(){
                override fun inflateView(parent: ViewGroup): View {
                    val v =  TextView(context)
                    v.gravity = Gravity.CENTER
                    v.text = "($x, $y)"
                    return v
                }
            }
        }

        return grid
    }

    fun apps(columns: Int, rows: Int): Grid{
        val grid = EmptyGrid(columns, rows)

        grid.forEachIndexed { x, y, cell ->
            grid[x, y] = BitmapIcon("($x, $y)", ActiveAppMock.Factory.list(context, 1)[0].bitmap)
        }

        return grid
    }
}
package io.rocketguys.dokey

import android.content.Context
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ListAdapter
import android.widget.ListPopupWindow


/**
 * TODO: Add class description
 *
 * @author Matteo Pellegrino matteo.pelle.pellegrino@gmail.com
 */
class NoFocusPopupMenu private constructor(context: Context) {
    private lateinit var popup: ListPopupWindow

    class Builder(val context: Context){
        private val items = linkedMapOf<String, View.OnClickListener?>()
        private var anchorView: View ?= null
        private val popup = ListPopupWindow(context)

        fun setAnchorView(anchor: View?): Builder{
            this.anchorView = anchor
            return this
        }

        fun addItem(text: String, onClick: ((View) -> Unit)? = null): Builder{
            items[text] = View.OnClickListener { onClick?.invoke(it) }
            return this
        }

        fun create(): NoFocusPopupMenu{
            val popupMenu = NoFocusPopupMenu(context)
            val adapter = ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, items.keys.toList())
            val measure = measureContentWidth(adapter)

            popup.anchorView = anchorView
            popup.setAdapter(adapter)
            popup.width = measure
            popup.horizontalOffset = -(measure/3)
            popup.setOnItemClickListener { _, view, position, _ ->
                popup.dismiss()
                items[items.keys.toList()[position]]?.onClick(view)
            }

            popupMenu.popup = popup
            return popupMenu
        }

        private fun measureContentWidth(listAdapter: ListAdapter): Int {
            var mMeasureParent: ViewGroup? = null
            var maxWidth = 0
            var itemView: View? = null
            var itemType = 0

            val widthMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            val heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            val count = listAdapter.count
            for (i in 0 until count) {
                val positionType = listAdapter.getItemViewType(i)
                if (positionType != itemType) {
                    itemType = positionType
                    itemView = null
                }

                if (mMeasureParent == null) {
                    mMeasureParent = FrameLayout(context)
                }

                itemView = listAdapter.getView(i, itemView, mMeasureParent)
                itemView!!.measure(widthMeasureSpec, heightMeasureSpec)

                val itemWidth = itemView.measuredWidth

                if (itemWidth > maxWidth) {
                    maxWidth = itemWidth
                }
            }

            return maxWidth
        }
    }

    fun show(){
        popup.show()
    }

}
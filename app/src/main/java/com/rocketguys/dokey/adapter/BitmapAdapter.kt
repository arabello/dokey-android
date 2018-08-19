package com.rocketguys.dokey.adapter

import android.graphics.Bitmap
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.rocketguys.dokey.R

/**
 * TODO: Add class description
 *
 * @author Matteo Pellegrino matteo.pelle.pellegrino@gmail.com
 */

class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imgView = itemView as ImageView
}

class BitmapAdapter(val bitmaps: Array<Bitmap>) : RecyclerView.Adapter<ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_active_app, parent, false))

    override fun getItemCount(): Int = bitmaps.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.imgView.setImageBitmap(bitmaps[position])
    }


}
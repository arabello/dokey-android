package io.rocketguys.dokey.sync

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import io.rocketguys.dokey.ActiveAppMock
import io.rocketguys.dokey.R
import io.rocketguys.dokey.network.NetworkManagerService
import kotlinx.android.synthetic.main.item_active_app.view.*

/**
 * TODO: Add class description
 *
 * @author Matteo Pellegrino matteo.pelle.pellegrino@gmail.com
 */

class ViewHolder(itemActiveApp: View) : RecyclerView.ViewHolder(itemActiveApp) {
    val imgView = itemActiveApp.icon!!
}

class ActiveAppAdapter(var activeApps: List<NetworkManagerService.App>) : RecyclerView.Adapter<ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_active_app, parent, false))

    override fun getItemCount(): Int = activeApps.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        activeApps[position].requestIcon { _, imageFile ->
            if (imageFile != null)
                Picasso.get().load(imageFile).into(holder.imgView)
        }
    }

}
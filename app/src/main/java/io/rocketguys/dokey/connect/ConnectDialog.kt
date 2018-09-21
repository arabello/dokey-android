package io.rocketguys.dokey.connect

import android.content.Context
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import io.rocketguys.dokey.R
import net.model.DeviceInfo

/**
 * This class provides a factory to create [AlertDialog]
 * mainly used in the [ConnectActivity] for connectivity issues.
 *
 * @author Matteo Pellegrino matteo.pelle.pellegrino@gmail.com
 */
class ConnectDialog {

    companion object {
        fun from(context: Context): Factory = Factory(context)
    }

    class Factory(context: Context) {

        private val builder = AlertDialog.Builder(context)
        private var txt: TextView
        private var img: ImageView
        private var dialog: AlertDialog

        init {
            val root = LayoutInflater.from(context).inflate(R.layout.dialog_connect, null)
            txt = root.findViewById(R.id.dlg_connect_txt)
            img = root.findViewById(R.id.dlg_connect_img)
            builder.setView(root)
            builder.setCancelable(false)
            builder.setTitle(R.string.dlg_connect_title)
            dialog = builder.create()
            root.findViewById<Button>(R.id.dlg_connect_btn).setOnClickListener {
                dialog.dismiss()
            }
        }

        fun createDialogInvalidQRCode(): AlertDialog{
            txt.setText(R.string.dlg_connect_InvalidQRCode)
            img.setImageResource(R.mipmap.dlg_connect_invalidqrcode)
            return dialog
        }

        fun createDialogOnServerNotInTheSameNetworkError(): AlertDialog{
            txt.setText(R.string.dlg_connect_OnServerNotInTheSameNetworkError)
            img.setImageResource(R.mipmap.dlg_connect_onservernotinthesamenetwork)
            return dialog
        }

        fun createDialogOnDesktopVersionTooLowError(serverInfo: DeviceInfo): AlertDialog {
            txt.text = builder.context.getString(R.string.dlg_connect_OnDesktopVersionTooLowError, serverInfo.name)
            img.setImageResource(R.mipmap.dlg_connect_ondesktopversiontoolow)
            return dialog
        }

        fun createDialogOnMobileVersionTooLowError(serverInfo: DeviceInfo): AlertDialog {
            txt.text = builder.context.getString(R.string.dlg_connect_OnMobileVersionTooLowError, serverInfo.name)
            img.setImageResource(R.mipmap.dlg_connect_onmobileversiontoolow)
            return dialog
        }
    }
}
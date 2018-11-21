package io.rocketguys.dokey.connect.usb

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class USBInstructionAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    companion object {
        const val ITEMS_NUM = 3
    }

    override fun getItem(position: Int): Fragment = USBInstructionFragment.newInstance(position)

    override fun getCount(): Int = ITEMS_NUM
}
package io.rocketguys.dokey.intro

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class IntroAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    companion object {
        const val ITEMS_NUM = 5
    }

    override fun getItem(position: Int): Fragment = IntroFragment.newInstance(position)

    override fun getCount(): Int = ITEMS_NUM
}
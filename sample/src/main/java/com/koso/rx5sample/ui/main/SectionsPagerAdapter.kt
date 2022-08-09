package com.koso.rx5sample.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.koso.rx5sample.R

private val TAB_TITLES = arrayOf(
    R.string.tab_text_conn,
    R.string.tab_text_navi,
    R.string.tab_text_free,
    R.string.tab_text_log
)

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return when (position) {
            0 -> {
                ConnectFragment.newInstance()
            }
            1 -> {
                NaviCommandsFragment.newInstance()
            }
            2 -> {
                FreeCommandsFragment.newInstance()
            }
            else -> {
                LogFragment.newInstance()
            }

        }

    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        return 4
    }
}
package org.autojs.autojs.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.autojs.autojs.ui.main.mall.MallManagerFragment
import org.autojs.autojs.ui.main.scripts.MyScriptListFragment
import org.autojs.autojs.ui.main.scripts.ScriptListFragment

class ViewPager3Adapter (
    fragmentActivity: FragmentActivity,
    private val scriptListFragment: MyScriptListFragment,
    private val mallManagerFragment: MallManagerFragment,
    ) : FragmentStateAdapter(fragmentActivity) {

        override fun createFragment(position: Int): Fragment {
            val fragment = when (position) {
                0 -> {
                    scriptListFragment
                }
                else -> {
                    mallManagerFragment
                }
            }
            return fragment
        }

        override fun getItemCount(): Int {
            return 2
        }
    }
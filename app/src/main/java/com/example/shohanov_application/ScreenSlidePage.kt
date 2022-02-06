package com.example.shohanov_application

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager


class ScreenSlidePage : Fragment() {
    private inner class ScreenSlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm,
        BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ) {

        override fun getCount(): Int = MyService.list_words_viewed.size

        override fun getItem(position: Int): Fragment{
            return CurrentWordFragment.newInstance(position)
        }

        override fun setPrimaryItem(
            container: ViewGroup,
            position: Int,
            `object`: Any
        ) {
            super.setPrimaryItem(container, position, `object`)
            val fragment =
                `object` as CurrentWordFragment
            if (fragment != mCurrentPrimaryItem) {
                mCurrentPrimaryItem=fragment
                fragment.play()
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_screen_slide_page, container, false)
        // Instantiate a ViewPager and a PagerAdapter.
        mPager = view.findViewById(R.id.pager)
        if(MyService.list_words_viewed.isEmpty()){
            MyService.get_new_worg_ranfom()
        }
        mPager?.adapter = ScreenSlidePagerAdapter(activity!!.supportFragmentManager)
        mPager?.currentItem = MyService.nom_list_viewed

        return view
    }

    companion object {
        var mPager: ViewPager? = null
        var mCurrentPrimaryItem: CurrentWordFragment? = null

        fun newInstance():ScreenSlidePage{
            return ScreenSlidePage()
        }
    }
}

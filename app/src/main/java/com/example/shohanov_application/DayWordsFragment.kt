package com.example.shohanov_application

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView


class DayWordsFragment(items: MutableList<Int>) : WordsFragment(items,R.layout.fragment_item_list) {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        // Set the adapter
        if (view is RecyclerView) {
            view.apply {
                adapter = WordsAdapter(items, listener)
            }
        }
        return view
    }

    companion object {
        var thisInstance:DayWordsFragment? = null

        fun newInstance(items: MutableList<Int>):DayWordsFragment{
            thisInstance?.let { return it }
            thisInstance=DayWordsFragment(items)
            return thisInstance!!
        }
    }

}

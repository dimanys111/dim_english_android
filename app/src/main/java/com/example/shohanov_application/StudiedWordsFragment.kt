package com.example.shohanov_application


class StudiedWordsFragment(items: MutableList<Int>) : WordsFragment(items,R.layout.fragment_item_list) {
    
    companion object {
        var thisInstance:StudiedWordsFragment? = null

        fun newInstance(items: MutableList<Int>):StudiedWordsFragment{
            thisInstance?.let { return it }
            thisInstance=StudiedWordsFragment(items)
            return thisInstance!!
        }
    }
}

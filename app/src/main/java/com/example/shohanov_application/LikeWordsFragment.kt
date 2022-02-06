package com.example.shohanov_application


class LikeWordsFragment(items: MutableList<Int>) : WordsFragment(items,R.layout.fragment_item_list) {

    companion object {
        var thisInstance:LikeWordsFragment? = null

        fun newInstance(items: MutableList<Int>):LikeWordsFragment{
            thisInstance?.let { return it }
            thisInstance=LikeWordsFragment(items)
            return thisInstance!!
        }
    }
}

package com.example.shohanov_application

class DifficultWordsFragment(items: MutableList<Int>) : WordsFragment(items,R.layout.fragment_item_list) {
    
    companion object {
        var thisInstance:DifficultWordsFragment? = null

        fun newInstance(items: MutableList<Int>):DifficultWordsFragment{
            thisInstance?.let { return it }
            thisInstance=DifficultWordsFragment(items)
            return thisInstance!!
        }
    }
}

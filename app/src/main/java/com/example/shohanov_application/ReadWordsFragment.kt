package com.example.shohanov_application





class ReadWordsFragment(items: MutableList<Int>) : WordsFragment(items,R.layout.fragment_item_list) {

    companion object {
        var thisInstance:ReadWordsFragment? = null

        fun newInstance(items: MutableList<Int>):ReadWordsFragment{
            thisInstance?.let { return it }
            thisInstance=ReadWordsFragment(items)
            return thisInstance!!
        }

        fun update() {
            thisInstance?.onUpdateData()
        }
    }
}

package com.example.shohanov_application

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager


class AllWordsFragment(items: MutableList<Int>) : WordsFragment(items) {

    var recyclerView: RecyclerView? =null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_item_list_all, container, false)

        recyclerView=view.findViewById(R.id.list)

        with(recyclerView as RecyclerView) {
            layoutManager =
                LinearLayoutManager(context)
            adapter = WordsAdapter(MyService.list_words_find, listener)
        }

        MainActivity.search?.setQuery(text_edit_text, false)

        return view
    }

    fun create_list_find_words(s: String) {
        text_edit_text = s

        if(s == ""){
            MyService.list_words_find=MyService.list_words_find_const.toMutableList()
            val wa = recyclerView?.adapter as WordsAdapter
            wa.items=MyService.list_words_find
        } else {
            MyService.list_words_find.clear()
            for (i in MyService.list_words_all_en_ru.indices) {
                if (MyService.list_words_all_en_ru[i].contains(s)) {
                    MyService.list_words_find.add(i)
                }
            }
        }
        recyclerView?.adapter?.notifyDataSetChanged()
    }

    companion object {
        var text_edit_text = ""
        var thisInstance:AllWordsFragment? = null

        fun newInstance(items: MutableList<Int>):AllWordsFragment{
            thisInstance?.let {
                it.create_list_find_words(text_edit_text)
                return it
            }
            thisInstance=AllWordsFragment(items)
            return thisInstance!!
        }
    }

}

package com.example.shohanov_application

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

open class WordsFragment(val items: MutableList<Int>,  val layout:Int=0) : Fragment(),OnUpdateDataListener {
    var root_view: RecyclerView? =null
    var listener: OnListFragmentInteractionListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(layout, container, false)
        // Set the adapter
        if (view is RecyclerView) {
            root_view=view
            with(view) {
                layoutManager =
                    LinearLayoutManager(context)
                adapter = DelWordsAdapter(items, listener,this@WordsFragment)
            }
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onUpdateData() {
        root_view?.adapter?.notifyDataSetChanged()
    }


}

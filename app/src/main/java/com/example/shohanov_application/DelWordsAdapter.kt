package com.example.shohanov_application

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_item_word_del.view.*

class DelWordsAdapter(private val items: MutableList<Int>,
                   private val mListener: OnListFragmentInteractionListener?,
                      private val onUpdateDataListener:OnUpdateDataListener
) : RecyclerView.Adapter<DelWordsAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener
    private val mOnClickListener_del: View.OnClickListener

    init {
        mOnClickListener_del = View.OnClickListener { v ->
            val item = v.tag as Int
            items.removeAt(item)
            if(items == MyService.list_words_viewed){
                MyService.list_words_viewed_sch.removeAt(item)
            }
            onUpdateDataListener.onUpdateData()
        }
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as Int
            mListener?.onListFragmentInteraction(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_item_word_del, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mIdView.text = (position+1).toString()
        val text=MyService.list_words_all_en_ru[items[position]]
        holder.mContentView.text = text
        with(holder.mButDel) {
            tag = position
            setOnClickListener(mOnClickListener_del)
        }
        with(holder.mView) {
            tag = items[position]
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(val mView: View) :RecyclerView.ViewHolder(mView) {
        val mButDel: TextView = mView.but_del_item_word
        val mIdView: TextView = mView.item_number_del
        val mContentView: TextView = mView.content_del
        override fun toString(): String {
            return super.toString() + " '" + mContentView.text + "'"
        }
    }
}
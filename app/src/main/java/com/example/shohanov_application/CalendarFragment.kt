package com.example.shohanov_application


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.GridView
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.util.*


class CalendarFragment : Fragment() {

    var rootView:View? =null
    var month: Calendar = Calendar.getInstance()
    var adapter: CalendarAdapter? = null


    companion object {
        var currentTime: Calendar? = null
        fun newInstance() = CalendarFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        currentTime = Calendar.getInstance()

        val view = inflater.inflate(R.layout.calendar, container, false)
        rootView = view
        month = Calendar.getInstance()
        adapter = CalendarAdapter(activity as Context, month)
        val gridview = view.findViewById<View>(R.id.gridview) as GridView
        gridview.adapter = adapter
        val title = view.findViewById<View>(R.id.title) as TextView
        title.text = DateFormat.format("MMMM yyyy", month)
        val previous = view.findViewById<View>(R.id.previous) as TextView
        previous.setOnClickListener {
            if (month.get(Calendar.MONTH) == month.getActualMinimum(Calendar.MONTH)) {
                month.set(
                    month.get(Calendar.YEAR) - 1,
                    month.getActualMaximum(Calendar.MONTH),
                    1
                )
            } else {
                month.set(Calendar.MONTH, month.get(Calendar.MONTH) - 1)
            }
            refreshCalendar()
        }
        val next = view.findViewById<View>(R.id.next) as TextView
        next.setOnClickListener {
            if (month.get(Calendar.MONTH) == month.getActualMaximum(Calendar.MONTH)) {
                month.set(
                    month.get(Calendar.YEAR) + 1,
                    month.getActualMinimum(Calendar.MONTH),
                    1
                )
            } else {
                month.set(Calendar.MONTH, month.get(Calendar.MONTH) + 1)
            }
            refreshCalendar()
        }
        gridview.onItemClickListener =
            AdapterView.OnItemClickListener { _, v, _, _ ->
                val text_date = v.findViewById<View>(R.id.date) as TextView
                if (text_date.text != "") {
                    MyService.selectDataString = month.get(Calendar.YEAR).toString()+"-"+month.get(Calendar.MONTH).toString()+"-"+text_date.text
                    DayWordsFragment.thisInstance=null
                    MainActivity.listener?.setNavMeny(5)
                }
            }

        return view
    }

    fun refreshCalendar() {
        val title = rootView?.findViewById<View>(R.id.title) as TextView
        adapter!!.refreshDays()
        adapter!!.notifyDataSetChanged()
        title.text = DateFormat.format("MMMM yyyy", month)
    }


}

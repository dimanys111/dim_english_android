package com.example.shohanov_application

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import java.util.*


class CalendarAdapter(val mContext: Context, private val month: Calendar) :
    BaseAdapter() {
    private val selectedDate: Calendar
    private var items: MutableList<String>?
    fun setItems(items: MutableList<String>) {
        for (i in items.indices) {
            if (items[i].length == 1) {
                items[i] = "0" + items[i]
            }
        }
        this.items = items
    }

    override fun getCount(): Int {
        return days.size
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    // create a new view for each item referenced by the Adapter
    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val v:View
        if (convertView == null) { // if it's not recycled, initialize some attributes
            val vi =
                mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            v = vi.inflate(R.layout.calendar_item, null)
        } else {
            v = convertView
        }

        val scale = mContext.getResources().getDisplayMetrics().density
        val pixels = (60 * scale + 0.5f).toInt()

        v.layoutParams = AbsListView.LayoutParams(GridView.AUTO_FIT, pixels)
        val dayView = v.findViewById<View>(R.id.date) as TextView
        val text_word_day = v.findViewById<View>(R.id.text_word_cal) as TextView
        // disable empty days from the beginning
        if (days[position] == "") {
            v.setBackgroundResource(R.drawable.list_item_background_clear)
            v.isClickable = false
            v.isFocusable = false
        } else { // mark current day as focused
            if (month[Calendar.YEAR] == selectedDate[Calendar.YEAR] &&
                month[Calendar.MONTH] == selectedDate[Calendar.MONTH] &&
                days[position] == "" + selectedDate[Calendar.DAY_OF_MONTH]
            ) {
                v.setBackgroundResource(R.drawable.list_item_background_cur)
                dayView.setTextColor(Color.parseColor("#000000"))
                text_word_day.setTextColor(Color.parseColor("#000000"))
            } else {
                v.setBackgroundResource(R.drawable.list_item_background)
                dayView.setTextColor(getColor(mContext, R.color.text_color))
                text_word_day.setTextColor(getColor(mContext, R.color.text_color))
            }
            val s = month.get(Calendar.YEAR).toString()+"-"+month.get(Calendar.MONTH).toString()+"-"+dayView.text
            var s1=""
            MyService.calendarMutableMap[s]?.let {
                var sch = 0
                for(i in it){
                    s1=s1+MyService.list_words_all_en[i]+"\n"
                    sch++
                    if(sch>1) {
                        s1 = s1.substring(0, s1.length - 1);
                        break
                    }
                }
            }
            text_word_day.text=s1
        }
        dayView.text = days[position]
        return v
    }

    fun refreshDays() { // clear items
        items!!.clear()
        val lastDay = month.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDay = month[Calendar.DAY_OF_WEEK]
        // figure size of the array
        days = if (firstDay == 1) {
            arrayOfNulls(lastDay + FIRST_DAY_OF_WEEK * 6)
        } else {
            arrayOfNulls(lastDay + firstDay - (FIRST_DAY_OF_WEEK + 1))
        }
        var j:Int
        // populate empty days before first real day
        if (firstDay > 1) {
            j = 0
            while (j < firstDay - FIRST_DAY_OF_WEEK) {
                days[j] = ""
                j++
            }
        } else {
            j = 0
            while (j < FIRST_DAY_OF_WEEK * 6) {
                days[j] = ""
                j++
            }
            j = FIRST_DAY_OF_WEEK * 6 + 1 // sunday => 1, monday => 7
        }
        // populate days
        var dayNumber = 1
        for (i in j - 1 until days.size) {
            days[i] = "" + dayNumber
            dayNumber++
        }
    }

    // references to our items
    var days: Array<String?> = arrayOfNulls(1)

    companion object {
        const val FIRST_DAY_OF_WEEK = 1 // Sunday = 0, Monday = 1
    }

    init {
        selectedDate = month.clone() as Calendar
        month[Calendar.DAY_OF_MONTH] = 1
        items = mutableListOf()
        refreshDays()
    }
}
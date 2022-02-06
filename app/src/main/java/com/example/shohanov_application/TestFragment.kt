package com.example.shohanov_application

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.gridlayout.widget.GridLayout
import kotlin.random.Random


class TestFragment : Fragment() {

    val but_arr:MutableList<TextView> = mutableListOf()
    var bool_first:Boolean = false
    val words_bool:MutableList<Boolean> = mutableListOf()
    var tv_fin:TextView? = null
    var list_words_viewed_test: MutableList<Int> = mutableListOf()
    var list_words_viewed_test_pair: MutableList<Pair<Boolean,Boolean>> = mutableListOf()
    var textView_test_word:TextView? = null
    val words_en:MutableList<String> = mutableListOf()
    val words_ru:MutableList<String> = mutableListOf()
    val rand_arr:MutableList<Int> = mutableListOf()
    var rand_cur:Int = 0
    var rand_int:Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_test, container, false)
        val tv_stop:TextView = view.findViewById(R.id.tv_stop)
        if(MyService.list_words_viewed.size<4){
            tv_stop.visibility=View.VISIBLE
        } else {
            tv_fin = view.findViewById(R.id.tv_fin)
            val grid_test: TableLayout = view.findViewById(R.id.tableLayout)

            textView_test_word = view.findViewById(R.id.textView_test_word)
            list_words_viewed_test=MyService.list_words_viewed.toMutableList()
            for(i in list_words_viewed_test.indices){
                list_words_viewed_test_pair.add(Pair(first = false, second = false))
            }

            val but_next_test: Button = view.findViewById(R.id.but_next_test)
            but_next_test.setOnClickListener {
                if(words_bool.size<MyService.list_words_viewed.size*2) {
                    if (bool_first) {
                        words_bool.add(false)
                    }
                }
                next_test()
            }

            for (i in 0 until grid_test.childCount) {
                val tr = grid_test.getChildAt(i) as TableRow
                for (j in 0 until tr.childCount) {
                    but_arr.add(tr.getChildAt(j) as TextView)
                    but_arr.last().setOnClickListener {
                        val v: TextView = it as TextView
                        if (but_arr.indexOf(it) == rand_int) {
                            v.setTextColor(Color.parseColor("#00FF00"))
                        } else {
                            v.setTextColor(Color.parseColor("#FF0000"))
                        }
                        if (bool_first) {
                            bool_first = false
                            words_bool.add(but_arr.indexOf(it) == rand_int)
                        }
                    }
                }
            }

            next_test()
        }

        return view
    }

    private fun next_test() {
        bool_first = true
        var n=0
        if(list_words_viewed_test.isEmpty()){
            for(i in words_bool){
                if(i){
                    n++
                }
            }

            tv_fin?.visibility=View.VISIBLE
            tv_fin?.text = "Правильных:"+n+" Неправильных:"+(words_bool.size-n)
            return
        }

        words_en.clear()
        words_ru.clear()
        rand_arr.clear()

        rand_cur=Random.nextInt(0, list_words_viewed_test.size)

        var rand_en_ru=Random.nextInt(0, 2)
        if(list_words_viewed_test_pair[rand_cur].first){
            rand_en_ru=1
            list_words_viewed_test_pair[rand_cur]=list_words_viewed_test_pair[rand_cur].copy(second = true)
        } else {
            if(list_words_viewed_test_pair[rand_cur].second){
                rand_en_ru=0
                list_words_viewed_test_pair[rand_cur]=list_words_viewed_test_pair[rand_cur].copy(first = true)
            } else {
                if(rand_en_ru==0) {
                    list_words_viewed_test_pair[rand_cur]=list_words_viewed_test_pair[rand_cur].copy(first = true)
                } else{
                    list_words_viewed_test_pair[rand_cur]=list_words_viewed_test_pair[rand_cur].copy(second = true)
                }
            }
        }

        for (i in but_arr.indices) {
            var k=Random.nextInt(0, MyService.list_words_viewed.size)
            while (rand_arr.contains(k)){
                k=Random.nextInt(0, MyService.list_words_viewed.size)
            }
            rand_arr.add(k)

            val word = MyService.list_words_all_en_ru[MyService.list_words_viewed[rand_arr[i]]]
            val xz = word.split(" - ")
            val word_en = xz[0]
            val word_ru = xz[1]
            words_en.add(word_en)
            words_ru.add(word_ru)
            if(rand_en_ru==0) {
                but_arr[i].text = word_en
            } else {
                but_arr[i].text = word_ru
            }
            but_arr[i].setTextColor(Color.parseColor("#000000"))
        }

        rand_int=-1
        for(i in rand_arr.indices){
            if(MyService.list_words_viewed[rand_arr[i]]==list_words_viewed_test[rand_cur]){
                rand_int=i
                break
            }
        }

        if(rand_int<0){
            rand_int = Random.nextInt(0, 4)
            val word = MyService.list_words_all_en_ru[list_words_viewed_test[rand_cur]]
            val xz = word.split(" - ")
            val word_en = xz[0]
            val word_ru = xz[1]
            words_en[rand_int] = word_en
            words_ru[rand_int] = word_ru
            if(rand_en_ru==0) {
                but_arr[rand_int].text = word_en
            } else {
                but_arr[rand_int].text = word_ru
            }
        }
        if(rand_en_ru==0) {
            textView_test_word?.text = words_ru[rand_int]
        } else {
            textView_test_word?.text = words_en[rand_int]
        }
        if(list_words_viewed_test_pair[rand_cur].first && list_words_viewed_test_pair[rand_cur].second) {
            list_words_viewed_test.removeAt(rand_cur)
            list_words_viewed_test_pair.removeAt(rand_cur)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            TestFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}

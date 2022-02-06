package com.example.shohanov_application


import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_current_word.view.*


class CurrentWordFragment(val pos:Int) : Fragment(),AsyncResponse {

    var root_view : View? = null
    var nom_word = -1
    var word_en = ""
    var xz = listOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_current_word, container, false)
        root_view=view
        set_curent_word()

        view.iv_sound?.setOnClickListener {
            MyService.play_mp3(word_en)
        }

        view.iv_studied.setOnClickListener {
            if(MyService.list_words_studied.contains(MyService.id_current_word)){
                view.iv_studied.setImageResource(R.drawable.ic_studied)
                MyService.list_words_studied.remove(nom_word)
            } else {
                view.iv_studied.setImageResource(R.drawable.ic_studied_red)
                MyService.list_words_studied.add(nom_word)
            }
            StudiedWordsFragment.thisInstance=null
        }

        view.iv_difficult.setOnClickListener {
            if(MyService.list_words_difficult.contains(MyService.id_current_word)){
                view.iv_difficult.setImageResource(R.drawable.ic_difficult)
                MyService.list_words_difficult.remove(nom_word)
            } else {
                view.iv_difficult.setImageResource(R.drawable.ic_difficult_red)
                MyService.list_words_difficult.add(nom_word)
            }
            DifficultWordsFragment.thisInstance=null
        }

        view.iv_like?.setOnClickListener {
            if(MyService.list_words_like.contains(MyService.id_current_word)) {
                view.iv_like.setImageResource(R.drawable.ic_like)
            } else {
                view.iv_like.setImageResource(R.drawable.ic_dis_like)
            }
            MyService.update_list_like(nom_word)
        }

        view.iv_next_word?.setOnClickListener {
            MyService.get_new_worg_ranfom()
            MyService.notif_close()
        }

        view.iv_google_transleter?.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.setPackage("com.google.android.apps.translate")

            val uri: Uri = Uri.Builder()
                .scheme("http")
                .authority("translate.google.com")
                .path("/m/translate")
                .appendQueryParameter(
                    "q",
                    word_en
                )
                .appendQueryParameter("tl", "ru") // target language
                .appendQueryParameter("sl", "en") // source language
                .build()
            intent.data = uri

            startActivity(intent)
        }

        return view
    }

    fun insert_str(str: String, color: String) : String{
        return  "    <html>\n" +
                "     <body>\n" +
                "     <p style=\"text-indent: 20px;text-align:justify;color:"+color+";\">"+str+"</p>\n" +
                "     </body>\n" +
                "    </html>\n"

    }

    init {
        nom_word=MyService.list_words_viewed[pos]
        val word = MyService.list_words_all.get(nom_word)
        xz=word.split("#")
        word_en = xz[1]
    }

    fun set_curent_word() {
        val word_trans = "[ "+xz[2]+" ]"
        val word_ru = xz[3]
        val word_jpg = xz[4]
        val word_text_en = xz[5]
        val word_text_ru = xz[6]
        val view=root_view!!

        val webView_en = WebView(activity)
        webView_en.isVerticalScrollBarEnabled = false
        webView_en.setBackgroundColor(Color.TRANSPARENT)
        view.linearLayout.addView(webView_en)
        webView_en.loadData(insert_str(word_text_en,"RGB(255, 119, 0)"), "text/html; charset=utf-8", "utf-8")


        webView_en.setOnTouchListener { view, motionEvent ->
            if(motionEvent.action==ACTION_UP) {
                val intent = Intent()
                intent.action = Intent.ACTION_VIEW
                intent.setPackage("com.google.android.apps.translate")

                val uri: Uri = Uri.Builder()
                    .scheme("http")
                    .authority("translate.google.com")
                    .path("/m/translate")
                    .appendQueryParameter(
                        "q",
                        word_text_en
                    )
                    .appendQueryParameter("tl", "ru") // target language
                    .appendQueryParameter("sl", "en") // source language
                    .build()
                intent.data = uri

                startActivity(intent)
            }

            true
        }



        val webView_ru = WebView(activity)
        webView_ru.isVerticalScrollBarEnabled = false
        webView_ru.setBackgroundColor(Color.TRANSPARENT)
        view.linearLayout.addView(webView_ru)
        webView_ru.loadData(insert_str(word_text_ru,"RGB(36, 205, 44)"), "text/html; charset=utf-8", "utf-8")

        view.tv_word_en?.text = word_en
        view.tv_word_ru?.text = word_ru
        view.tv_word_trans?.text = word_trans
//        view.tv_word_text_en?.text = word_text_en
//        view.tv_word_text_ru?.text = word_text_ru

        if(MyService.list_words_studied.contains(nom_word)){
            view.iv_studied.setImageResource(R.drawable.ic_studied_red)
        } else {
            view.iv_studied.setImageResource(R.drawable.ic_studied)
        }

        if (MyService.list_words_like.contains(nom_word)) {
            view.iv_like.setImageResource(R.drawable.ic_dis_like)
        } else {
            view.iv_like.setImageResource(R.drawable.ic_like)
        }

        if (MyService.list_words_difficult.contains(nom_word)) {
            view.iv_difficult.setImageResource(R.drawable.ic_difficult_red)
        } else {
            view.iv_difficult.setImageResource(R.drawable.ic_difficult)
        }

        MyService.get_bitmap(this,word_en,word_jpg)?.let { view.iv_word.setImageBitmap(it) }
    }

    fun play() {
        if (MyService.bool_sound_every_word) {
            if(word_en != "") {
                MyService.play_mp3(word_en)
            }
        }
    }

    companion object {
        fun newInstance(pos:Int):CurrentWordFragment{
            return CurrentWordFragment(pos)
        }
    }

    override fun processFinish(result:Bitmap?) {
        view?.iv_word?.setImageBitmap(result)
    }
}

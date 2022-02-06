package com.example.shohanov_application

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.IdRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*

interface OnUpdateDataListener {
    // TODO: Update argument type and name
    fun onUpdateData()
}

interface OnListFragmentInteractionListener {
    // TODO: Update argument type and name
    fun onListFragmentInteraction(item: Int)
}

fun hideKeyboard(activity: Activity) {
    val imm: InputMethodManager =
        activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    //Find the currently focused view, so we can grab the correct window token from it.
    var view = activity.currentFocus
    //If no view currently has focus, create a new one, just so we can grab a window token from it
    if (view == null) {
        view = View(activity)
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,OnListFragmentInteractionListener {

    private val idItems:MutableList<MenuItem> = mutableListOf()
    private var curItem:MenuItem? = null

    private var searchMenuItem: MenuItem? = null

    fun isStoragePermissionGranted():Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            true
        }
    }

    override fun onStart() {
        super.onStart()
        MyService.getCutentDataString()
    }

    override fun onStop() {
        super.onStop()
        MyService.save_SharedPreferences()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        listener=this

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        MyService.init()
        MyService.alarm_start()
        MyService.alarm_testing()
        //isStoragePermissionGranted()
        if(intent.action==MyService.TESTING_WORD_ACTION){
            setNavMeny(7)
        } else {
            setNavMeny(0)
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            if(idItems.isEmpty())
                super.onBackPressed()
            else{
                onNavigationItemSelected(idItems.last())
                idItems.remove(idItems.last())
                idItems.remove(idItems.last())
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        hideKeyboard(this)
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)

        searchMenuItem = menu.findItem(R.id.search)
        searchMenuItem?.setVisible(false)
        search = searchMenuItem?.actionView as SearchView

        search?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                TODO("Not yet implemented")
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                AllWordsFragment.thisInstance?.create_list_find_words(newText!!)

                return true
            }

        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.search -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        searchMenuItem?.isVisible = false
        curItem?.let {
            if(!idItems.contains(it))
                idItems.add(it)
        }

        hideKeyboard(this)
        // Создадим новый фрагмент
        var fragment: Fragment? = null
        // Handle navigation view item clicks here.
        try {
            when (item.itemId) {
                R.id.nav_new_word -> {
                    fragment = ScreenSlidePage.newInstance()
                }
                R.id.nav_list_words_all -> {
                    searchMenuItem?.isVisible = true
                    fragment = AllWordsFragment.newInstance(MyService.list_words_find)
                }
                R.id.nav_list_words_vievs -> {
                    fragment = ReadWordsFragment.newInstance(MyService.list_words_viewed)
                }
                R.id.nav_list_words_like -> {
                    fragment = LikeWordsFragment.newInstance(MyService.list_words_like)
                }
                R.id.nav_tools -> {
                    fragment = SettingsFragment.newInstance()
                }
                R.id.nav_test_word -> {
                    fragment = TestFragment.newInstance()
                }
                R.id.nav_calendar -> {
                    fragment = CalendarFragment.newInstance()
                }
                R.id.nav_list_words_difficult -> {
                    fragment = DifficultWordsFragment.newInstance(MyService.list_words_difficult)
                }
                R.id.nav_list_words_studied -> {
                    fragment = StudiedWordsFragment.newInstance(MyService.list_words_studied)
                }
                R.id.nav_list_words_day -> {
                    MyService.calendarMutableMap[MyService.selectDataString]?.let {
                        fragment = DayWordsFragment.newInstance(it)
                    }
                }
                R.id.nav_share -> {

                }
                R.id.nav_send -> {

                }
            }
        } catch (e:Exception) {
            e.printStackTrace();
        }

        curItem = item

        // Вставляем фрагмент, заменяя текущий фрагмент
        val fragmentManager = supportFragmentManager
        fragment?.let { fragmentManager.beginTransaction().replace(R.id.container, it)
            .commit() }
        // Выделяем выбранный пункт меню в шторке
        item.isChecked = true
        // Выводим выбранный пункт в заголовке
        title = item.title

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onListFragmentInteraction(item: Int) {
        MyService.get_word_num(item)
        MyService.update_widget()
        setNavMeny(0)
    }

    fun setNavMeny(i:Int) {
        onNavigationItemSelected(nav_view.menu.getItem(i))
    }

    companion object {
        var search:SearchView? = null

        var listener:MainActivity? = null
    }
}

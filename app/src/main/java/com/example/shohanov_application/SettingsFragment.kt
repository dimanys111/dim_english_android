package com.example.shohanov_application

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager


class SettingsFragment: PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity)
        sharedPref.registerOnSharedPreferenceChangeListener(this)
    }

    companion object {
        val key_cb_notif_sound:String="cb_notif_sound"
        val key_cb_notif_visib:String="cb_notif_visib"
        val key_seekbar_time_notif_min:String="seekbar_time_notif_min"
        val key_seekbar_time_notif_max:String="seekbar_time_notif_max"
        val key_list_time_notif_show:String="list_time_notif_show"
        val key_switch_sound_word:String="switch_sound_word"
        val key_switch_sound_every_word:String="switch_sound_every_word"


        fun newInstance() = SettingsFragment()
    }

    override fun onSharedPreferenceChanged(p0: SharedPreferences?, p1: String?) {
        if (p1==key_cb_notif_visib) {
            p0?.getBoolean(key_cb_notif_visib, true)?.let{
                MyService.bool_notif_visibl = it
            }
            if(!MyService.bool_notif_visibl) {
                MyService.notif_and_alarm_close()
            } else {
                MyService.alarm_start()
            }
        }

        if (p1==key_cb_notif_sound) {
            p0?.getBoolean(key_cb_notif_sound, false)?.let{
                MyService.bool_notif_sound = it
            }
            MyService.building_notification=null
        }

        if (p1==key_switch_sound_word) {
            p0?.getBoolean(key_switch_sound_word, false)?.let{
                MyService.bool_sound_word = it
            }
        }

        if (p1==key_switch_sound_every_word) {
            p0?.getBoolean(key_switch_sound_every_word, false)?.let{
                MyService.bool_sound_every_word = it
            }
        }

        if (p1==key_list_time_notif_show) {
            val s = p0?.getString(key_list_time_notif_show, "")
            MyService.set_str_time(s!!)
        }

        if (p1==key_seekbar_time_notif_min) {
            val i = p0?.getInt(key_seekbar_time_notif_min, 0)
            MyService.set_min_time(i!!)
        }
        if (p1==key_seekbar_time_notif_max) {
            val i = p0?.getInt(key_seekbar_time_notif_max, 0)
            MyService.set_max_time(i!!)
        }
    }
}
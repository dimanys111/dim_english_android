package com.example.shohanov_application

import android.app.*
import android.appwidget.AppWidgetManager
import android.content.*
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences.Editor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.preference.PreferenceManager
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import kotlin.random.Random


class MyBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            MyService.init()
            MyService.alarm_testing()
            MyService.alarm_start()
            MyService.update_widget()
        }
        if (MyService.LIKE_WORD_ACTION == intent.action) {
            MyService.init()
            MyService.update_list_like(MyService.id_current_word)
        }
        if (MyService.NEXT_WORD_ACTION == intent.action) {
            MyService.init()
            MyService.get_new_worg_ranfom()
            MyService.notif_close()
        }
        if (MyService.NEXT_WORD_NOTIF_ACTION == intent.action) {
            MyService.init()
            MyService.get_new_worg_ranfom()
            MyService.building_notification=null
            MyService.notification_curent_show()
        }
        if (MyService.PLAY_WORD_ACTION == intent.action) {
            MyService.init()
            MyService.play_mp3(MyService.word_en)
        }
    }
}

class AlarmTimerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        MyService.init()
        if(MyService.prov_alarm()){
            MyService.sendNotifReceiver()
        } else {
            MyService.alarm_wake_up()
        }
    }
}

class AlarmTestingReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        MyService.notif_test_show()
    }
}

interface AsyncResponse {
    fun processFinish(result:Bitmap?)
}

class MyAsyncTaskJpeg:AsyncTask<Pair<String,String>, Void, Bitmap?>() {
    var delegate: AsyncResponse? = null
    override fun doInBackground(vararg params: Pair<String,String>): Bitmap? {
        try {
            val url = URL(params[0].first)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.setDoInput(true)
            connection.connect()
            val `in` = connection.getInputStream()
            val result: Bitmap = BitmapFactory.decodeStream(`in`)
            val new_rez = Bitmap.createScaledBitmap(result, result.width*2, result.height*2, false)
            try {
                val file=File(MyService.word_dir,params[0].second + ".jpg")
                FileOutputStream(file).use({ out ->
                    result.compress(Bitmap.CompressFormat.JPEG, 100, out) // bmp is your Bitmap instance
                })
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return new_rez
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    override fun onPostExecute(result: Bitmap?) {
        delegate?.processFinish(result)
    }
}

class MyAsyncTaskMp3:AsyncTask<Pair<String,String>, Void, File?>() {
    private val BUFFER_SIZE = 4096
    override fun doInBackground(vararg params: Pair<String,String>): File? {
        try {
            val url = URL(params[0].first)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.setDoInput(true)
            connection.connect()
            val inputStream = connection.getInputStream()
            val file=File(MyService.word_dir,params[0].second + ".mp3")
            val outputStream: OutputStream = FileOutputStream(file)
            var bytesRead = -1
            val buffer = ByteArray(BUFFER_SIZE)
            while (inputStream.read(buffer).also({ bytesRead = it }) != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.flush()
            outputStream.close()
            return file
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    override fun onPostExecute(result: File?) {
        result?.let { MyService.mp3_play(it) }
    }
}

class MyService {

    companion object {
        var building_notification: Notification? = null
        var notificationManagerCompat:NotificationManagerCompat? = null
        private val NOTIFICATION_ID = 234
        private val NOTIFICATION_TEST_ID = 134
        private val CHANNEL_ID = "my_channel"
        private val CHANNEL_ID_SOUND = "my_channel_sound"
        val PLAY_WORD_ACTION = "ru.PLAY_WORD_ACTION"
        val NEXT_WORD_ACTION = "ru.NEXT_WORD_ACTION"
        val NEXT_WORD_NOTIF_ACTION = "ru.NEXT_WORD_NOTIF_ACTION"
        val LIKE_WORD_ACTION = "ru.LIKE_WORD_ACTION"

        val TESTING_WORD_ACTION = "ru.TESTING_WORD_ACTION"

        var id_current_word:Int=0
        var nom_list_viewed:Int=0
        var word:String = ""
        var word_en:String = ""
        var word_trans:String = ""
        var word_jpg:String = ""
        var word_text_en:String = ""
        var word_text_ru:String = ""
        var word_ru:String = ""
        var image: Bitmap? =null

        var calendarMutableMap : MutableMap<String, MutableList<Int>> = mutableMapOf()

        var list_words_viewed_sch: MutableList<Int> = mutableListOf()
        var list_words_viewed: MutableList<Int> = mutableListOf()
        var list_words_like: MutableList<Int> = mutableListOf()
        var list_words_find: MutableList<Int> = mutableListOf()
        var list_words_find_const: MutableList<Int> = mutableListOf()
        var list_words_difficult: MutableList<Int> = mutableListOf()
        var list_words_studied: MutableList<Int> = mutableListOf()
        val list_words_all: MutableList<String> = mutableListOf()
        val list_words_all_en_ru: MutableList<String> = mutableListOf()
        val list_words_all_en: MutableList<String> = mutableListOf()
        var word_dir: File? = null

        private val LEN_PREFIX = "Count_"
        private val VAL_PREFIX = "IntValue_"

        var LIST_LIKE_WORDS_SAVE:String = "LIST_LIKE_WORDS_SAVE"
        var LIST_VIEW_WORDS_SAVE:String = "LIST_VIEW_WORDS_SAVE"
        var LIST_SCH_VIEW_WORDS_SAVE:String = "LIST_SCH_VIEW_WORDS_SAVE"
        var LIST_DIFF_WORDS_SAVE:String = "LIST_DIFF_WORDS_SAVE"
        var LIST_STUD_WORDS_SAVE:String = "LIST_STUD_WORDS_SAVE"

        var bool_notif_visibl:Boolean = true

        var bool_sound_word:Boolean = false
        var bool_sound_every_word:Boolean = false

        var bool_notif_sound:Boolean = false
        var str_time_alarm:String="0"
        var id_viewed:Int=-1

        var int_time_min_alarm:Int=0
        var int_time_max_alarm:Int=0

        var cutentDataString:String = ""
        var selectDataString:String = ""

        class Delegate:AsyncResponse{
            override fun processFinish(result:Bitmap?) {
                image=result
                notification_show_word()
            }
        }

        var delegate: Delegate? = Delegate()


        fun play_mp3(word_en:String) {
            try {
                val file = File(word_dir, word_en+".mp3")
                if(!file.exists()) {
                    val asyncTask = MyAsyncTaskMp3()
                    asyncTask.execute(Pair("https://studynow.ru/assets/illust/words/" + word_en + ".mp3",word_en))
                } else {
                    mp3_play(file)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        fun mp3_play(file: File) {
            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(file.toString())
            mediaPlayer.setOnPreparedListener {
                mediaPlayer.start()
            }
            mediaPlayer.prepareAsync()
        }

        fun update_list_like(nom_current_word:Int) {
            if (list_words_like.contains(nom_current_word)) {
                list_words_like.remove(nom_current_word)
            } else {
                list_words_like.add(nom_current_word)
            }
            LikeWordsFragment.thisInstance=null
            update_widget()
        }

        fun sendNotifReceiver()
        {
            notification_curent_show()
            update_widget()
        }

        fun creatNotificationManagerCompat()
        {
            if(notificationManagerCompat==null) {
                update_notificationManager()
            }
        }

        fun update_notificationManager() {
            notificationManagerCompat = NotificationManagerCompat.from(MyApplication.appContext)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                create_channel_soind()
                create_channel()
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun create_channel_soind() {
            val name = "channel_sound"
            val Description = "This is my channel sound"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(CHANNEL_ID_SOUND, name, importance)
            val audioAttributes = AudioAttributes.Builder().build()
            val notifSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            mChannel.setSound(notifSound, audioAttributes)
            mChannel.description = Description
            mChannel.enableLights(true)
            mChannel.enableVibration(true)
            notificationManagerCompat?.createNotificationChannel(mChannel)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun create_channel() {
            val name = "channel"
            val Description = "This is my channel"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            mChannel.setSound(null, null)
            mChannel.description = Description
            mChannel.enableLights(true)
            mChannel.enableVibration(true)
            notificationManagerCompat?.createNotificationChannel(mChannel)
        }

        const val FIRST_DAY_OF_WEEK = 1 // Sunday = 0, Monday = 1

        fun alarm_testing() {
            val alarmManager = MyApplication.appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val calendar = Calendar.getInstance()
            val day_of_week=calendar.get(Calendar.DAY_OF_WEEK)-FIRST_DAY_OF_WEEK
            var day=calendar.get(Calendar.DAY_OF_MONTH)
            var month=calendar.get(Calendar.MONTH)
            var year=calendar.get(Calendar.YEAR)
            val h=calendar.get(Calendar.HOUR_OF_DAY)
            var h_alarm=10
            var min = 0
            if(day_of_week<6 || (day_of_week==6 && h<10)) {
                day += (6 - day_of_week)
            } else {
                if(day_of_week>6) {
                    day +=6
                }
            }
            if (day > calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                month += 1
                if (month > 11) {
                    year += 1
                    month = 0
                }
                day = 1
            }
            calendar.set(year,month,day,
                h_alarm,min)
            val time = calendar.timeInMillis
            alarmManager.set(AlarmManager.RTC_WAKEUP, time, getPendAlarmTesting())
        }

        fun alarm_wake_up() {
            val alarmManager = MyApplication.appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val calendar = Calendar.getInstance()
            var day=calendar.get(Calendar.DAY_OF_MONTH)
            var month=calendar.get(Calendar.MONTH)
            var year=calendar.get(Calendar.YEAR)
            val h=calendar.get(Calendar.HOUR_OF_DAY)
            if(int_time_min_alarm<int_time_max_alarm && h>int_time_min_alarm) {
                day += 1
                if(day>calendar.getActualMaximum(Calendar.DAY_OF_MONTH)){
                    month += 1
                    if(month>11){
                        year +=1
                        month = 0
                    }
                    day = 1
                }
            }
            calendar.set(year,month,day,
                int_time_min_alarm,0)
            val time = calendar.timeInMillis
            val time_delta=AlarmManager.INTERVAL_HOUR / 60 * str_time_alarm.toInt()
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, time,time_delta, getPendAlarm())
        }

        fun prov_alarm():Boolean {
            val calendar = Calendar.getInstance()
            val h=calendar.get(Calendar.HOUR_OF_DAY)
            if(int_time_max_alarm<int_time_min_alarm){
                return (h >= int_time_min_alarm || h < int_time_max_alarm)
            } else {
                return (h in int_time_min_alarm until int_time_max_alarm)
            }
        }

        fun alarm_start() {
            if(prov_alarm()){
                alarm_timer()
            } else {
                alarm_wake_up()
            }
        }

        private fun alarm_timer() {
            val calendar = Calendar.getInstance()
            val alarmManager =
                MyApplication.appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            calendar.add(Calendar.MINUTE, str_time_alarm.toInt())
            val time = calendar.timeInMillis
            val time_delta = AlarmManager.INTERVAL_HOUR / 60 * str_time_alarm.toInt()
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                time,
                time_delta,
                getPendAlarm()
            )
        }

        private fun alarm_stop() {
            val alarmManager = MyApplication.appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(getPendAlarm())
        }

        private fun getPendAlarm(): PendingIntent? {
            val intent = Intent(MyApplication.appContext, AlarmTimerReceiver::class.java)
            return PendingIntent.getBroadcast(
                MyApplication.appContext,
                192,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        private fun getPendAlarmTesting(): PendingIntent? {
            val intent = Intent(MyApplication.appContext, AlarmTestingReceiver::class.java)
            return PendingIntent.getBroadcast(
                MyApplication.appContext,
                182,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        fun set_min_time(i:Int) {
            int_time_min_alarm=i
            alarm_start()
        }

        fun set_max_time(i:Int) {
            int_time_max_alarm=i
            alarm_start()
        }

        fun set_str_time(s:String) {
            str_time_alarm=s
            alarm_start()
        }

        fun notif_and_alarm_close() {
            notif_close()
            alarm_stop()
        }

        fun notif_close() {
            building_notification=null
            notificationManagerCompat?.cancel(NOTIFICATION_ID)
            notificationManagerCompat=null
        }

        fun notification_word():Notification{
            save_SharedPreferences()
            val nextIntent = Intent(MyApplication.appContext, MyBroadcastReceiver::class.java)
            nextIntent.action=NEXT_WORD_NOTIF_ACTION
            val nextPendingIntent = PendingIntent.getBroadcast(MyApplication.appContext, 0, nextIntent, 0)

            val resultIntent = Intent(MyApplication.appContext, MainActivity::class.java)
            val stackBuilder = TaskStackBuilder.create(MyApplication.appContext)
            stackBuilder.addNextIntentWithParentStack(resultIntent)
            val resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

            val playIntent = Intent(MyApplication.appContext, MyBroadcastReceiver::class.java)
            playIntent.action=PLAY_WORD_ACTION
            val playPendingIntent = PendingIntent.getBroadcast(MyApplication.appContext, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            var CHANNEL_ID_=CHANNEL_ID
            if(bool_notif_sound){
                CHANNEL_ID_= CHANNEL_ID_SOUND
            }

            val builder = NotificationCompat.Builder(MyApplication.appContext, CHANNEL_ID_)
                .setSmallIcon(R.drawable.ic_a)
                .setContentTitle(word_en)
                .setContentText(word_ru)
                .setContentIntent(resultPendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .addAction(R.drawable.ic_play, "PLAY",
                    playPendingIntent)
                .addAction(R.drawable.ic_next, "Дальше",
                    nextPendingIntent)
//            .setStyle(MediaStyle()
//                .setShowActionsInCompactView(0))
                .setLargeIcon(image)
                .setStyle(NotificationCompat.BigPictureStyle()
                    .bigPicture(image)
                    .bigLargeIcon(null)
                )
            if(bool_notif_sound){
                val notifSound : Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                builder.setDefaults(Notification.DEFAULT_SOUND).setSound(notifSound)
            } else {
                builder.setDefaults(Notification.DEFAULT_VIBRATE)
            }

            building_notification=builder.build()
            return building_notification!!
        }

        fun notif_test_show()
        {
            creatNotificationManagerCompat()
            notificationManagerCompat?.notify(NOTIFICATION_TEST_ID, notification_testing())
        }

        fun notification_testing():Notification{

            val resultIntent = Intent(MyApplication.appContext, MainActivity::class.java)
            resultIntent.action=TESTING_WORD_ACTION
            val stackBuilder = TaskStackBuilder.create(MyApplication.appContext)
            stackBuilder.addNextIntentWithParentStack(resultIntent)
            val resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)


            var CHANNEL_ID_=CHANNEL_ID
            if(bool_notif_sound){
                CHANNEL_ID_= CHANNEL_ID_SOUND
            }

            val builder = NotificationCompat.Builder(MyApplication.appContext, CHANNEL_ID_)
                .setSmallIcon(R.drawable.ic_a)
                .setContentTitle("Пора тестироваться!")
                .setContentText("Может тест")
                .setContentIntent(resultPendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MIN)
            if(bool_notif_sound){
                val notifSound : Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                builder.setDefaults(Notification.DEFAULT_SOUND).setSound(notifSound)
            } else {
                builder.setDefaults(Notification.DEFAULT_VIBRATE)
            }

            return builder.build()
        }

        fun notification():Notification?{
            image?.let { return notification_word()  }
            image=get_bitmap(delegate, word_en, word_jpg)
            image?.let { return notification_word() }
            return null
        }

        fun notification_curent_show()
        {
            if(bool_sound_every_word){
                play_mp3(word_en)
            }
            creatNotificationManagerCompat()
            if(bool_notif_visibl){
                if(building_notification!=null)
                    notificationManagerCompat?.notify(NOTIFICATION_ID, building_notification!!)
                else
                    notification()?.let{notificationManagerCompat?.notify(NOTIFICATION_ID, it)}
            }
        }

        fun notification_show_word()
        {
            if(bool_notif_visibl){
                notificationManagerCompat?.notify(NOTIFICATION_ID, notification_word())
            }
        }

        fun get_bitmap(delegate: AsyncResponse?,word_en:String,word_jpg:String): Bitmap? {
            val file = File(word_dir, word_en + ".jpg")
            if (!file.exists()) {
                val asyncTask = MyAsyncTaskJpeg()
                asyncTask.delegate = delegate
                asyncTask.execute(Pair("https://studynow.ru/assets/illust/smallpics/" + word_jpg + ".jpg",word_en))
            } else {
                val options = BitmapFactory.Options()
                options.inPreferredConfig = Bitmap.Config.ARGB_8888
                val new_rez =
                    BitmapFactory.decodeFile(file.toString(), options)?.let { Bitmap.createScaledBitmap(it, it.width*2, it.height*2, false) }
                return new_rez
            }
            return null
        }

        fun storeIntArray(name: String, array: MutableList<Int>) {
            val edit: Editor = PreferenceManager.getDefaultSharedPreferences(MyApplication.appContext).edit()
            edit.putInt(LEN_PREFIX + name, array.size)
            var count = 0
            for (i in array) {
                edit.putInt(VAL_PREFIX + name + count++, i)
            }
            edit.apply()
        }

        fun getFromPrefs(name: String): MutableList<Int> {
            val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.appContext)
            val count = prefs.getInt(LEN_PREFIX + name, 0)
            val ret:  MutableList<Int> = mutableListOf()
            for (i in 0 until count) {
                ret.add(prefs.getInt(VAL_PREFIX + name + i, i))
            }
            return ret
        }

        fun get_current_word() {
            if(list_words_viewed.isEmpty()){
                get_new_worg_ranfom()
            } else {
                get_word_num(list_words_viewed.last(),list_words_viewed.size-1)
            }
        }

        fun get_word_num(id_word: Int, nom_viewed: Int=-1) {
            if(nom_viewed<0){
                nom_list_viewed = list_words_viewed.indexOf(id_word)
            } else {
                nom_list_viewed = nom_viewed
            }
            image=null
            id_current_word=id_word
            word = list_words_all.get(id_word)
            val xz=word.split("#")
            word_en = xz[1]
            word_trans = "[ "+xz[2]+" ]"
            word_ru = xz[3]
            word_jpg = xz[4]
            word_text_en = xz[5]
            word_text_ru = xz[6]
            word=word_en+" - "+word_ru
        }

        fun get_new_worg_ranfom(){
            if (!replay_words())
                create_rand()
            if(bool_sound_word && !bool_sound_every_word){
                play_mp3(word_en)
            }
            alarm_start()
            update_widget()
        }

        private fun create_rand() {
            var rand_int: Int
            if (list_words_viewed.isEmpty()
                || id_current_word == list_words_viewed.last()
                || id_current_word == id_viewed
            ) {
                rand_int = Random.nextInt(0, list_words_all.size)
                while (list_words_viewed.contains(rand_int)) {
                    rand_int = Random.nextInt(0, list_words_all.size)
                }
                add_word_to_list_viewed(rand_int)
            } else {
                rand_int = list_words_viewed.last()
            }
            get_word_num(rand_int, list_words_viewed.size - 1)
            ScreenSlidePage.mPager?.setCurrentItem(list_words_viewed.size - 1)
        }

        private fun replay_words(): Boolean {
            for (i in list_words_viewed_sch.indices) {
                if (!list_words_studied.contains(list_words_viewed[i])) {
                    if (list_words_difficult.contains(list_words_viewed[i])) {
                        if (prov_replay_word(i, 5)) return true
                    } else {
                        if (prov_replay_word(i, 10)) return true
                    }
                }
            }
            for (i in list_words_viewed_sch.indices) {
                list_words_viewed_sch[i] = list_words_viewed_sch[i] + 1
            }
            return false
        }

        private fun prov_replay_word(i: Int, ii: Int): Boolean {
            if (list_words_viewed_sch[i] % ii == 0) {
                list_words_viewed_sch[i] = list_words_viewed_sch[i] + 1
                id_viewed = list_words_viewed[i]
                get_word_num(id_viewed, i)
                ScreenSlidePage.mPager?.setCurrentItem(i)
                return true
            }
            return false
        }

        fun add_word_to_list_viewed(rand_int: Int) {
            list_words_viewed.add(rand_int)
            list_words_viewed_sch.add(1)
            ReadWordsFragment.update()
            set_word_to_calendar(rand_int)
            ScreenSlidePage.mPager?.adapter?.notifyDataSetChanged()
        }

        fun set_word_to_calendar(rand_int: Int) {
            getCutentDataString()
            if (!calendarMutableMap.containsKey(cutentDataString)) {
                calendarMutableMap[cutentDataString] = mutableListOf()
            }
            calendarMutableMap[cutentDataString]!!.add(rand_int)
        }

        fun getCutentDataString() {
            val calendar = Calendar.getInstance()
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)
            cutentDataString = year.toString()+"-"+month.toString()+"-"+day.toString()
            selectDataString = cutentDataString
        }

        fun save_SharedPreferences() {
            storeIntArray(LIST_VIEW_WORDS_SAVE, list_words_viewed)
            storeIntArray(LIST_LIKE_WORDS_SAVE, list_words_like)
            storeIntArray(LIST_SCH_VIEW_WORDS_SAVE, list_words_viewed_sch)
            storeIntArray(LIST_DIFF_WORDS_SAVE, list_words_difficult)
            storeIntArray(LIST_STUD_WORDS_SAVE, list_words_studied)

            val file = File(MyApplication.appContext.getDir("data", MODE_PRIVATE), "map")
            val outputStream = ObjectOutputStream(FileOutputStream(file))
            outputStream.writeObject(calendarMutableMap)
            outputStream.flush()
            outputStream.close()
        }

        fun update_widget()
        {
            val intent = Intent(MyApplication.appContext, MyWidget::class.java)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids: IntArray = AppWidgetManager.getInstance(MyApplication.appContext).
                getAppWidgetIds(ComponentName(MyApplication.appContext, MyWidget::class.java))
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            MyApplication.appContext.sendBroadcast(intent)
        }

        fun init() {
            if(list_words_all.isEmpty()) {
                word_dir = File(MyApplication.appContext.getDir("data", MODE_PRIVATE), "words")
                if(!word_dir!!.exists()){
                    word_dir!!.mkdirs()
                }
                val assetManager = MyApplication.appContext.getAssets()
                val input = assetManager.open("listfile.txt")
                val reader = BufferedReader(InputStreamReader(input))
                while (true) {
                    val line = reader.readLine() ?: break
                    list_words_all.add(line)
                    val xz=line.split("#")
                    list_words_all_en_ru.add(xz[1]+" - "+xz[3])
                    list_words_all_en.add(xz[1])
                }

                list_words_find_const=list_words_all.indices.toMutableList()
                list_words_find=list_words_all.indices.toMutableList()


                val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(MyApplication.appContext)
                bool_notif_visibl = prefs.getBoolean(SettingsFragment.key_cb_notif_visib, true)
                bool_notif_sound = prefs.getBoolean(SettingsFragment.key_cb_notif_sound, false)
                bool_sound_word = prefs.getBoolean(SettingsFragment.key_switch_sound_word, false)
                bool_sound_every_word = prefs.getBoolean(SettingsFragment.key_switch_sound_every_word, false)
                str_time_alarm = prefs.getString(SettingsFragment.key_list_time_notif_show, "2")!!
                int_time_min_alarm = prefs.getInt(SettingsFragment.key_seekbar_time_notif_min, 7)
                int_time_max_alarm = prefs.getInt(SettingsFragment.key_seekbar_time_notif_max, 23)

                val file = File(MyApplication.appContext.getDir("data", MODE_PRIVATE), "map")
                if(file.exists()) {
                    val inputStream = ObjectInputStream(FileInputStream(file))
                    calendarMutableMap =
                        inputStream.readObject() as MutableMap<String, MutableList<Int>>
                    inputStream.close()
                }

                list_words_viewed = getFromPrefs(LIST_VIEW_WORDS_SAVE)
                list_words_viewed_sch = getFromPrefs(LIST_SCH_VIEW_WORDS_SAVE)
                list_words_like = getFromPrefs(LIST_LIKE_WORDS_SAVE)
                list_words_difficult = getFromPrefs(LIST_DIFF_WORDS_SAVE)
                list_words_studied = getFromPrefs(LIST_STUD_WORDS_SAVE)
                get_current_word()
            }
        }
    }
}

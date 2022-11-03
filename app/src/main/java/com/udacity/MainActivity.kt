package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.*
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0
    private lateinit var downloadManager: DownloadManager
    private var timer: Timer? = Timer()

    private lateinit var notificationManager: NotificationManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager

        initAndRegisterReceiver()

        initButtonClickListener()

        createChannel()

        notificationManager = ContextCompat.getSystemService(
            this,
            NotificationManager::class.java
        ) as NotificationManager

    }

    private fun initButtonClickListener() {
        custom_button.setOnClickListener {
            val url = when (download_radio_group.checkedRadioButtonId) {
                R.id.download_glide -> Constants.GLIDE_URL
                R.id.download_repo -> Constants.REPO_URL
                R.id.download_retrofit -> Constants.RETROFIT_URL
                else -> ""
            }
            val fileName = getString(
                when (download_radio_group.checkedRadioButtonId) {
                    R.id.download_glide -> R.string.glide
                    R.id.download_repo -> R.string.repo
                    else -> R.string.retrofit
                }
            )
            if (url.isNotEmpty()) {
                custom_button.buttonState = ButtonState.DOWNLOADING
                download(url, fileName)
            } else {
                Toast.makeText(this, getString(R.string.no_selection), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initAndRegisterReceiver() {
        val filter = IntentFilter()
        filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        filter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED)
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            custom_button.buttonState = ButtonState.IDLE
            stopTimer()
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == intent?.action) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                val query = DownloadManager.Query()
                query.setFilterById(id)
                val cursor = downloadManager.query(query)
                if (cursor.moveToFirst()) {
                    if (cursor.count > 0) {
                        val status =
                            cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                        val file =
                            cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE))

                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            // So something here on Success.
                            Toast.makeText(this@MainActivity, getString(R.string.success), Toast.LENGTH_SHORT).show()
                            notificationManager.sendNotification(true, file, applicationContext)
                        } else {
                            // So something here on failed.
                            Toast.makeText(this@MainActivity, getString(R.string.fail), Toast.LENGTH_SHORT).show()
                            notificationManager.sendNotification(false, file, applicationContext)
                        }

                    }
                }
                cursor.close()
            }
        }
    }

    private fun download(url: String, fileName: String) {
        val request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle(fileName)
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
        initStatusObserver(downloadID)
    }

    private fun initStatusObserver(id: Long) {
        timer = Timer()
        val query = DownloadManager.Query()
        query.setFilterById(id)
        val cursor = downloadManager.query(query)
        val timerTask: TimerTask = object : TimerTask() {
            override fun run() {
                if (cursor.moveToFirst()) {
                    if (cursor.count > 0) {
                        val status =
                            cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                        if (status == DownloadManager.STATUS_PENDING) {
                            downloadManager.remove(downloadID)
                            stopTimer()
                            MainScope().launch {
                                val file =
                                    cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE))
                                notificationManager.sendNotification(false, file, applicationContext)

                                Toast.makeText(this@MainActivity, getString(R.string.timeout), Toast.LENGTH_SHORT).show()
                                custom_button.buttonState = ButtonState.IDLE
                                cursor.close()
                            }
                        }else if (status == DownloadManager.STATUS_SUCCESSFUL){
                            stopTimer()
                        }
                    }
                }
            }
        }
        timer?.scheduleAtFixedRate(timerTask, (30 * 1000).toLong(), (30 * 1000).toLong())

    }

    private fun stopTimer(){
        if (timer != null){
            timer?.cancel()
            timer = null
        }
    }
    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                getString(R.string.notification_channel_id),
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = getString(R.string.notification_channel_desc)

            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)

        }
    }

}

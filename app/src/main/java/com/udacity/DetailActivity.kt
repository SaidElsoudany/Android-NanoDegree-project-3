package com.udacity

import android.app.NotificationManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)
        toolbar.title = getString(R.string.title_activity_detail)

        intent.extras.apply {
            file_name_desc_text.text = this?.getString("title")
            val notificationId = this?.getInt("notificationId")
            val success = this?.getBoolean("status")
            if (success == true){
                status_desc_text.text = getString(R.string.success)
                status_desc_text.setTextColor(resources.getColor(R.color.colorPrimaryDark, theme))
            }else{
                status_desc_text.text = getString(R.string.fail)
                status_desc_text.setTextColor(resources.getColor(android.R.color.holo_red_dark, theme))
            }
            val notificationManager = ContextCompat.getSystemService(
                this@DetailActivity,
                NotificationManager::class.java
            ) as NotificationManager
            notificationManager.cancelNotification(notificationId!!)
        }

        ok_btn.setOnClickListener{
            finish()
        }
    }

}

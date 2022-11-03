/*
 * Copyright (C) 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.udacity

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import java.util.*



fun NotificationManager.sendNotification(status: Boolean, fileName: String, applicationContext: Context) {
    val notificationId = SystemClock.uptimeMillis().toInt()
    val detailIntent = Intent(applicationContext, DetailActivity::class.java)
    detailIntent.putExtra("status", status)
    detailIntent.putExtra("title", fileName)
    detailIntent.putExtra("notificationId", notificationId)

    val detailPendingIntent = PendingIntent.getActivity(
        applicationContext,
        notificationId,
        detailIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )


    val builder = NotificationCompat.Builder(
        applicationContext,
        applicationContext.getString(R.string.notification_channel_id)
    )

        .setSmallIcon(R.drawable.ic_assistant_black_24dp)
        .setContentTitle(applicationContext
            .getString(R.string.notification_title))
        .setContentText(applicationContext.getString(R.string.notification_description))
        .setAutoCancel(true)

        .addAction(
            NotificationCompat.Action(null,
                applicationContext.getString(R.string.notification_button),
                detailPendingIntent)
        )
        .setPriority(NotificationCompat.PRIORITY_HIGH)

    notify(notificationId, builder.build())
}

fun NotificationManager.cancelNotification(id: Int) {
    cancel(id)
}





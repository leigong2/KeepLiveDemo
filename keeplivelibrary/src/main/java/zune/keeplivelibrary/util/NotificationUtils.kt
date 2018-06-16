package zune.keeplivelibrary.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.support.v4.app.NotificationCompat

import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build.VERSION_CODES.O
import android.support.annotation.RequiresApi
import zune.keeplivelibrary.R

/**
 * Created by leigong2 on 2018-04-06 006.
 */

class NotificationUtils(private val mContext: Context) {

    private var manager: NotificationManager? = null

    fun createNotificationChannel() {
        var channel: NotificationChannel? = null
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH)
            getManager().createNotificationChannel(channel)
        }
    }

    private fun getManager(): NotificationManager {
        if (manager == null) {
            manager = mContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        }
        return manager as NotificationManager
    }

    fun getChannelNotification(title: String, content: String): Notification.Builder? {
        return if (Build.VERSION.SDK_INT >= O) {
            Notification.Builder(mContext, id)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setSmallIcon(R.mipmap.icon_round)
                    .setAutoCancel(true)
        } else null
    }

    fun getNotification_25(title: String, content: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(mContext)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.mipmap.icon_round)
                .setAutoCancel(true)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    fun sendNotification(title: String, content: String){
        if (Build.VERSION.SDK_INT >= O) {
            createNotificationChannel()
            val notification = getChannelNotification(title, content)!!.build()
            notification.flags = Notification.FLAG_NO_CLEAR
            getManager().notify(1, notification)
        } else {
            val notification = getNotification_25(title, content).build()
            getManager().notify(1, notification)
        }
    }

    companion object {
        val id = "channel_1"
        val name = "channel_name_1"
    }
}

package zune.keeplivelibrary.util

import android.app.*
import android.content.Context
import android.os.Build
import android.support.v4.app.NotificationCompat

import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build.VERSION_CODES.O
import android.support.annotation.RequiresApi
import com.blankj.utilcode.util.SPUtils
import zune.keeplivelibrary.R
import zune.keeplivelibrary.activity.OnePointActivity

/**
 * Created by leigong2 on 2018-04-06 006.
 */

class NotificationUtils() {
    private var mContext: Context? = null
    var notification:NotificationBean = NotificationBean()
    constructor(context: Context) : this() {
        this@NotificationUtils.mContext = context
        notification.title = SPUtils.getInstance().getString("notification_title", "保活")
        notification.content = SPUtils.getInstance().getString("notification_content", "保活")
        notification.resId = SPUtils.getInstance().getInt("notification_resid", R.mipmap.icon_round)
    }

    fun setNotification(title:String, content:String, resId:Int) {
        notification.title = title
        notification.content = content
        notification.resId = resId
        SPUtils.getInstance().put("notification_title",title)
        SPUtils.getInstance().put("notification_content",content)
        SPUtils.getInstance().put("notification_resid",resId)
    }

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
            manager = mContext?.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        }
        return manager as NotificationManager
    }

    fun getChannelNotification(): Notification.Builder? {
        return if (Build.VERSION.SDK_INT >= O) {
            Notification.Builder(mContext, id)
                    .setContentTitle(notification.title)
                    .setContentText(notification.content)
                    .setSmallIcon(notification.resId)
                    .setAutoCancel(true)
        } else null
    }

    fun getNotification_25(): NotificationCompat.Builder {
        return NotificationCompat.Builder(mContext)
                .setContentTitle(notification.title)
                .setContentText(notification.content)
                .setSmallIcon(notification.resId)
                .setAutoCancel(true)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    fun sendNotification(){
        if (Build.VERSION.SDK_INT >= O) {
            createNotificationChannel()
            val notification = getChannelNotification()!!.build()
            notification.flags = Notification.FLAG_NO_CLEAR
            getManager().notify(1, notification)
        } else {
            val notification = getNotification_25().build()
            getManager().notify(1, notification)
        }
    }

    fun sendNotification(service: Service) {
        val intent = Intent(service, OnePointActivity::class.java)
        val pi = PendingIntent.getActivity(service, 0, intent, 0)
        val notification = NotificationCompat.Builder(service)
                .setContentTitle(notification.title)
                .setContentText(notification.content)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(notification.resId)
                .setLargeIcon(BitmapFactory.decodeResource(service.resources, notification.resId))
                .setContentIntent(pi)
                .build()
        service.startForeground(1, notification)
    }

    companion object {
        val id = "channel_1"
        val name = "channel_name_1"
    }

    inner class NotificationBean {
        var resId:Int = 0
        var title: String? = null
        var content: String? = null
    }
}

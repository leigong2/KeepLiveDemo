package zune.keeplivelibrary.service

import android.app.AlarmManager
import android.app.AlarmManager.RTC
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Build.VERSION_CODES.N
import android.os.Build.VERSION_CODES.O
import android.os.IBinder
import android.os.PowerManager
import android.support.v4.app.NotificationCompat
import android.util.Log
import zune.keeplivelibrary.R
import zune.keeplivelibrary.activity.OnePointActivity
import zune.keeplivelibrary.service.BaseOService.Companion.ALARM_INTERVAL
import zune.keeplivelibrary.service.high.RemoteOService
import zune.keeplivelibrary.service.low.RemoteService
import zune.keeplivelibrary.util.AlarmSendUtil
import zune.keeplivelibrary.util.CrashHandler
import zune.keeplivelibrary.util.NotificationUtils

/**
 * Created by Fussen on 2017/2/21.
 *
 *
 * 需要正常工作的服务
 */
class SimpleService : Service() {
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, SimpleService::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startService(intent)
        }
    }
    override fun onCreate() {
        println("zune: onCreateO${this.javaClass.simpleName}")
        if (Build.VERSION.SDK_INT >= O) {
            val notificationUtils = NotificationUtils(this)
            notificationUtils.sendNotification("保活", "保活")
        } else {
            val intent = Intent(this, OnePointActivity::class.java)
            val pi = PendingIntent.getActivity(this, 0, intent, 0)
            val notification = NotificationCompat.Builder(this)
                    .setContentTitle("保活")
                    .setContentText("保活")
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.mipmap.icon_round)
                    .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.icon_round))
                    .setContentIntent(pi)
                    .build()
            startForeground(1, notification)
        }
        super.onCreate()
    }

    /**
     * 最近任务列表中划掉卡片时回调
     */
    override fun onTaskRemoved(rootIntent: Intent) {
        onEnd(rootIntent)
        Log.i("zune: ", "划掉卡片")
    }

    /**
     * 设置-正在运行中停止服务时回调
     */
    override fun onDestroy() {
        onEnd(null)
        println("zune: onDestroy${this.javaClass.simpleName}")
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun onEnd(rootIntent: Intent?) {
        AlarmSendUtil.instanse.sendCallLiveBroadcast(this@SimpleService, 0)
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        //true为打开，false为关闭
        val ifOpen = powerManager.isScreenOn
        if (!CrashHandler.isAppaLive(this, "zune.keeplivelibrary") && !ifOpen) {
            val intent = Intent(this, OnePointActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        Log.i("zune: ", "SimpleService保存数据到磁盘")
        if (android.os.Build.VERSION.SDK_INT >= N) {
            (applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager)
                    .set(RTC, 500L + System.currentTimeMillis()
                            , PendingIntent.getService(this, 2
                            , Intent(this, SimpleService::class.java)
                            , PendingIntent.FLAG_ONE_SHOT))  //134217728
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        println("zune: onStartCommandO${this.javaClass.simpleName}")
        if (Build.VERSION.SDK_INT < N) {
            try {
                RemoteService.start(this)
            } catch (e: Exception) {
                Log.i("zune: ", "不允许后台启动")
            }
        } else {
            RemoteOService.start(application, ALARM_INTERVAL)
        }
        return Service.START_STICKY
    }
}

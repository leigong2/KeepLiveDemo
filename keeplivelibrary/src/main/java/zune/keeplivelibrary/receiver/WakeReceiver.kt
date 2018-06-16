package com.zune.test180311.low

import android.app.Notification
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION_CODES.N
import android.os.IBinder
import android.util.Log

import android.os.Build.VERSION_CODES.O
import zune.keeplivelibrary.service.BaseOService.Companion.ALARM_INTERVAL
import zune.keeplivelibrary.service.high.MainOService
import zune.keeplivelibrary.service.low.MainService


class WakeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (GRAY_WAKE_ACTION == action && Build.VERSION.SDK_INT < O) {
            Log.i("zune:", "唤起广播")
            val wakeIntent = Intent(context, WakeNotifyService::class.java)
            context.startService(wakeIntent)
        }
    }

    /**
     * 用于其他进程来唤醒UI进程用的Service
     */
    class WakeNotifyService : Service() {
        override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
            if (Build.VERSION.SDK_INT < 18) {
                startForeground(WAKE_SERVICE_ID, Notification())//API < 18 ，此方法能有效隐藏Notification上的图标
            } else if (Build.VERSION.SDK_INT < O) {
                val innerIntent = Intent(this, WakeGrayInnerService::class.java)
                startService(innerIntent)
                startForeground(WAKE_SERVICE_ID, Notification())
            }
            return Service.START_STICKY
        }

        override fun onBind(intent: Intent): IBinder? {
            // TODO: Return the communication channel to the service.
            throw UnsupportedOperationException("Not yet implemented")
        }
    }

    /**
     * 给 API >= 18 的平台上用的灰色保活手段
     */
    class WakeGrayInnerService : Service() {

        override fun onCreate() {
            super.onCreate()
        }

        override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
            Log.i("zune:", "唤醒pushServer")

            if (Build.VERSION.SDK_INT < N) {
                MainService.start(this)
            } else {
                MainOService.start(application, ALARM_INTERVAL)
            }
            //stopForeground(true);
            stopSelf()
            return super.onStartCommand(intent, flags, startId)
        }

        override fun onBind(intent: Intent): IBinder? {
            // TODO: Return the communication channel to the service.
            throw UnsupportedOperationException("Not yet implemented")
        }

        override fun onDestroy() {
            Log.i(TAG, "InnerService -> onDestroy")
            super.onDestroy()
        }
    }

    companion object {

        private val TAG = WakeReceiver::class.java.simpleName
        private val WAKE_SERVICE_ID = -1111

        /**
         * 灰色保活手段唤醒广播的action
         */
        val GRAY_WAKE_ACTION = "com.wake.gray"
    }
}

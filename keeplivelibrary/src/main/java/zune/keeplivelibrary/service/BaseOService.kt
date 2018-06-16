package zune.keeplivelibrary.service

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.support.v4.app.JobIntentService
import android.util.Log
import com.xiaomi.mipush.sdk.MiPushClient
import com.zune.test180311.low.ScreenBroadcastListener
import com.zune.test180311.low.WakeReceiver
import org.greenrobot.eventbus.EventBus
import zune.keeplivelibrary.R
import zune.keeplivelibrary.activity.OnePointActivity
import zune.keeplivelibrary.app.KeepLiveHelper
import zune.keeplivelibrary.config.ConstantsConfig
import zune.keeplivelibrary.event.ServiceDeadEvent
import zune.keeplivelibrary.service.high.AidlOService
import zune.keeplivelibrary.service.high.MainOService
import zune.keeplivelibrary.service.high.RemoteOService

/**
 * Created by leigong2 on 2018-06-09 009.
 */
abstract class BaseOService:JobIntentService() {
    private var mMediaPlayer: MediaPlayer? = null
    private var WAKE_REQUEST_CODE = 6666
    companion object {
        /**
         * 定时唤醒的时间间隔，8s - 20s(60s / 8  -  60s / 3)
         */
        var ALARM_INTERVAL = 1 * 60 * 1000L

    }
    private var listener: ScreenBroadcastListener.ScreenStateListener? = null
    @SuppressLint("WakelockTimeout")
    override fun onCreate() {
        super.onCreate()
        println("zune: onCreateO${this.javaClass.simpleName}")
        if (ConstantsConfig.musicToggle) {
            /*zune： play 一个静音资源**/
            mMediaPlayer = MediaPlayer.create(applicationContext, R.raw.silent)
            mMediaPlayer?.isLooping = true
        }
        if (ConstantsConfig.screenToggle) {
            registScreen()
        }
        if (ConstantsConfig.pushToggle) {
            MiPushClient.enablePush(this)
        }
        if (ConstantsConfig.alarmToggle) {
            //发送唤醒广播来促使挂掉的UI进程重新启动起来
            restartDelay()
        }
    }

    override fun onHandleWork(intent: Intent) {
        println("zune: onHandleWork${this.javaClass.simpleName}")
        if (ConstantsConfig.screenToggle) {
            registScreen()
        }
        if (ConstantsConfig.pushToggle) {
            MiPushClient.enablePush(this)
        }
        if (ConstantsConfig.alarmToggle) {
            //发送唤醒广播来促使挂掉的UI进程重新启动起来
            restartDelay()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("zune: onStartCommandO${this.javaClass.simpleName}")
        if (ConstantsConfig.musicToggle) {
            stopPlayMusic()
        }
        if (ConstantsConfig.screenToggle) {
            registScreen()
        }
        if (ConstantsConfig.pushToggle) {
            MiPushClient.enablePush(this)
        }
        if (ConstantsConfig.alarmToggle) {
            //发送唤醒广播来促使挂掉的UI进程重新启动起来
            restartDelay()
        }
        return Service.START_STICKY
    }

    override fun stopService(name: Intent?): Boolean {
        println("zune: stopServiceO${this.javaClass.simpleName}")
        return super.stopService(name)
    }

    override fun onDestroy() {
        super.onDestroy()
        onEnd(null)
        println("zune: onDestroyO${this.javaClass.simpleName}")
    }

    /**
     * 最近任务列表中划掉卡片时回调
     */
    override fun onTaskRemoved(rootIntent: Intent) {
        onEnd(rootIntent)
    }

    protected open fun onEnd(rootIntent: Intent?) {
        Log.i("zune: ", "保存数据到磁盘")
        if (ConstantsConfig.remoteToggle) {
            MainOService.start(this, ALARM_INTERVAL)
            RemoteOService.start(this, ALARM_INTERVAL)
            AidlOService.start(this, ALARM_INTERVAL)
        }
        if (ConstantsConfig.musicToggle) {
            Thread(Runnable {
                startPlayMusic()
            }).start()
        }
        EventBus.getDefault().post(ServiceDeadEvent())
    }

    private fun restartDelay() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(this, MainOService::class.java)
        alarmIntent.action = WakeReceiver.GRAY_WAKE_ACTION
        val operation = PendingIntent.getBroadcast(this, WAKE_REQUEST_CODE, alarmIntent
                , PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                , ALARM_INTERVAL, operation)
    }

    protected fun startPlayMusic() {
        Log.d("zune:", "启动后台播放音乐")
        mMediaPlayer?.start()
    }

    protected fun stopPlayMusic() {
        Log.d("zune:", "关闭后台播放音乐")
        mMediaPlayer?.stop()
    }

    private fun registScreen() {
        if (listener == null) {
            listener = object : ScreenBroadcastListener.ScreenStateListener {
                override fun onScreenOn() {
                    println("zune: 开屏 开启activity")
                    KeepLiveHelper.screenOn = true
                    OnePointActivity.start(this@BaseOService, true)
                }

                override fun onScreenOff() {
                    println("zune: 锁屏 开启activity")
                    KeepLiveHelper.screenOn = false
                    OnePointActivity.start(this@BaseOService)
                }
            }
        }
        ScreenBroadcastListener.getInstance(this)
                .registerListener(listener!!)
    }
}

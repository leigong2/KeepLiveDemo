package zune.keeplivelibrary.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import com.xiaomi.mipush.sdk.MiPushClient
import com.zune.test180311.low.ScreenBroadcastListener
import com.zune.test180311.low.WakeReceiver
import com.zune.test180311.util.AlarmSendUtil
import org.greenrobot.eventbus.EventBus
import zune.keeplivelibrary.R
import zune.keeplivelibrary.activity.OnePointActivity
import zune.keeplivelibrary.app.KeepLiveHelper
import zune.keeplivelibrary.config.ConstantsConfig
import zune.keeplivelibrary.event.ServiceDeadEvent
import zune.keeplivelibrary.service.low.AidlService
import zune.keeplivelibrary.service.low.MainService
import zune.keeplivelibrary.service.low.RemoteService

/**
 * Created by leigong2 on 2018-06-09 009.
 */
abstract class BaseService:Service(){
    private var mMediaPlayer: MediaPlayer? = null
    private var WAKE_REQUEST_CODE = 6666
    /**
     * 定时唤醒的时间间隔，3分钟 - 10分钟
     */
    private var ALARM_INTERVAL = 1 * 60 * 1000L
    private var listener: ScreenBroadcastListener.ScreenStateListener? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("zune: onStartCommand${this.javaClass.simpleName}")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun stopService(name: Intent?): Boolean {
        println("zune: stopService${this.javaClass.simpleName}")
        return super.stopService(name)
    }

    override fun onDestroy() {
        super.onDestroy()
        println("zune: onDestroy${this.javaClass.simpleName}")
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
            AlarmSendUtil.instanse.sendCallLiveBroadcast(this@BaseService, 0)
            try {
                MainService.start(this)
                RemoteService.start(this)
                AidlService.start(this)
            } catch (e: Exception) {
                Log.i("zune: ", "不允许后台开启")
            }

        }
        if (ConstantsConfig.musicToggle) {
            Thread(Runnable {
                startPlayMusic()
            }).start()
        }
        EventBus.getDefault().post(ServiceDeadEvent())
    }

    protected fun startPlayMusic() {
        Log.d("zune:", "启动后台播放音乐")
        mMediaPlayer?.start()
    }

    protected fun stopPlayMusic() {
        Log.d("zune:", "关闭后台播放音乐")
        mMediaPlayer?.stop()
    }

    private fun restartDelay() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(this, MainService::class.java)
        alarmIntent.action = WakeReceiver.GRAY_WAKE_ACTION
        val operation = PendingIntent.getBroadcast(this, WAKE_REQUEST_CODE, alarmIntent
                , PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                , ALARM_INTERVAL, operation)
    }

    private fun registScreen() {
        if (listener == null) {
            listener = object : ScreenBroadcastListener.ScreenStateListener {
                override fun onScreenOn() {
                    println("zune: 开屏 开启activity")
                    KeepLiveHelper.screenOn = true
                    OnePointActivity.start(this@BaseService, true)
                }

                override fun onScreenOff() {
                    println("zune: 锁屏 开启activity")
                    KeepLiveHelper.screenOn = false
                    OnePointActivity.start(this@BaseService)
                }
            }
        }
        ScreenBroadcastListener.getInstance(this)
                .registerListener(listener!!)
    }
}
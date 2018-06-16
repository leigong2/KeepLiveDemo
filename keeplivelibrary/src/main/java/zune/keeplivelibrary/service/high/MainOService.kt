package zune.keeplivelibrary.service.high

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import org.greenrobot.eventbus.EventBus
import zune.keeplivelibrary.config.ConstantsConfig
import zune.keeplivelibrary.event.ServiceDeadEvent
import zune.keeplivelibrary.service.BaseOService

/**
 * Created by leigong2 on 2018-06-07 007.
 */
class MainOService: BaseOService() {
    companion object {
        var JOB_ID = 1002
        fun start(context: Context) {
            val intent = Intent()
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            enqueueWork(context, MainOService::class.java, JOB_ID, intent)
        }
        fun start(context: Context, delay:Long) {
            Handler().postDelayed({ start(context) }, delay)
        }
        fun activityStart(activity: Activity) {
            activity.startService(Intent(activity, MainOService::class.java))
        }
    }

    override fun onEnd(rootIntent: Intent?) {
        println("zune: onEnd${this.javaClass.simpleName}")
        if (ConstantsConfig.remoteToggle) {
            RemoteOService.start(this, ALARM_INTERVAL)
        }
        if (ConstantsConfig.aidlToggle) {
            AidlOService.start(this, ALARM_INTERVAL)
        }
        if (ConstantsConfig.musicToggle) {
            Thread(Runnable {
                startPlayMusic()
            }).start()
        }
        EventBus.getDefault().post(ServiceDeadEvent())
    }
}
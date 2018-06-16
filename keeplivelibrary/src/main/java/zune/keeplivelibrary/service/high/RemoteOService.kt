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
class RemoteOService: BaseOService() {
    companion object {
        var JOB_ID = 1003
        fun start(context: Context) {
            enqueueWork(context, RemoteOService::class.java, JOB_ID, Intent())
        }
        fun start(context: Context, delay:Long) {
            Handler().postDelayed({ start(context) }, delay)
        }
        fun activityStart(activity:Activity) {
            activity.startService(Intent(activity, RemoteOService::class.java))
        }
    }
    override fun onEnd(rootIntent: Intent?) {
        MainOService.start(this, ALARM_INTERVAL)
        if (ConstantsConfig.musicToggle) {
            Thread(Runnable {
                startPlayMusic()
            }).start()
        }
        EventBus.getDefault().post(ServiceDeadEvent())
    }
}
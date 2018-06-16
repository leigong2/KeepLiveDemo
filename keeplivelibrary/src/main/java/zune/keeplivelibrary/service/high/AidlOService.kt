package zune.keeplivelibrary.service.high

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import zune.keeplivelibrary.IProgressAidlInterface
import zune.keeplivelibrary.service.BaseOService

/**
 * Created by leigong2 on 2018-06-09 009.
 */
class AidlOService: BaseOService() {
    companion object {
        var JOB_ID = 1001
        fun start(context: Context) {
            enqueueWork(context, AidlOService::class.java, JOB_ID, Intent())
        }
        fun start(context: Context, delay:Long) {
            Handler().postDelayed({ start(context) }, delay)
        }
        fun activityStart(activity: Activity) {
            activity.startService(Intent(activity, AidlOService::class.java))
        }
    }
    private var myBinder: ProgressAidlBinder? = null
    override fun onCreate() {
        super.onCreate()
        myBinder = ProgressAidlBinder("AidlService")
    }

    override fun onBind(intent: Intent): IBinder? {
        return myBinder
    }
    inner class ProgressAidlBinder(name:String): IProgressAidlInterface.Stub() {
        var name = name
        override fun getServiceName(): String {
            return name
        }
    }
}
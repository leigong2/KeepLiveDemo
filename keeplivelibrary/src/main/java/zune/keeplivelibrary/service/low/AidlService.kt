package zune.keeplivelibrary.service.low

import android.content.Context
import android.content.Intent
import android.os.IBinder
import zune.keeplivelibrary.IProgressAidlInterface
import zune.keeplivelibrary.service.BaseService

/**
 * Created by leigong2 on 2018-06-09 009.
 */
class AidlService: BaseService() {
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, AidlService::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startService(intent)
        }
    }
    var myBinder: ProgressAidlBinder? = null
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
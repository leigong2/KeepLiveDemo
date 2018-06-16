package keeplive.zune.com.keeplivedemo

import android.content.Intent
import android.support.v4.app.JobIntentService

/**
 * Created by leigong2 on 2018-06-16 016.
 */
class BindOService: JobIntentService() {
    override fun onHandleWork(intent: Intent) {

    }

    override fun onCreate() {
        super.onCreate()
        println("zune: 测试bindservice")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("zune: 测试bindservice start")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        println("zune: 测试bindservice onDestroy")
    }
}
package keeplive.zune.com.keeplivedemo

import android.annotation.SuppressLint
import android.app.Application
import android.os.Build.VERSION_CODES.N
import zune.keeplivelibrary.app.KeepLiveHelper


/**
 * Created by leigong2 on 2018-06-07 007.
 */
class MainApp:Application(){
    var PUSH_APP_ID = "2882303761517566170"
    var PUSH_APP_KEY = "5121756688170"
    companion object {
        @SuppressLint("StaticFieldLeak")
        var context: MainApp? = null
        internal fun getApp(): MainApp {
            return context!!
        }
    }
    override fun onCreate() {
        super.onCreate()
        context = this
        KeepLiveHelper.getDefault().init(this, PUSH_APP_ID, PUSH_APP_KEY)
        if (android.os.Build.VERSION.SDK_INT >= N) {
            KeepLiveHelper.getDefault().bindService(BindOService::class.java)
        } else {
            KeepLiveHelper.getDefault().bindService(BindService::class.java)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        KeepLiveHelper.getDefault().onTerminate()
    }
}
package zune.keeplivelibrary.receiver
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.Utils
import zune.keeplivelibrary.activity.OnePointActivity
import zune.keeplivelibrary.app.KeepLiveHelper
import zune.keeplivelibrary.util.CrashHandler

/**
 * Created by leigong2 on 2018-04-05 005.
 */

class CallLiveBroadCastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intentBundle: Intent) {
        Log.i("zune: ", "广播接收者")
        if (!CrashHandler.isServiceRunning(context,
                        "zune.keeplivelibrary.service.high.MainOService")
                && !CrashHandler.isServiceRunning(context,
                        "zune.keeplivelibrary.service.low.MainService")) {
            KeepLiveHelper.getDefault().init(Utils.getApp()
                    , SPUtils.getInstance().getString("PUSH_APP_ID")
                    , SPUtils.getInstance().getString("PUSH_APP_KEY"))
            OnePointActivity.start(context, true)
        }
    }
}

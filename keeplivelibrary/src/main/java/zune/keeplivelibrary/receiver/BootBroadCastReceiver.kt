package com.zune.test180311.main


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.Utils
import zune.keeplivelibrary.activity.OnePointActivity
import zune.keeplivelibrary.app.KeepLiveHelper

class BootBroadCastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        println("zune: 开机启动BootBroadCastReceiver")
        if (intent.action.toString().equals(Intent.ACTION_BOOT_COMPLETED)) {
            KeepLiveHelper.getDefault().init(Utils.getApp()
                    , SPUtils.getInstance().getString("PUSH_APP_ID")
                    , SPUtils.getInstance().getString("PUSH_APP_KEY"))
            OnePointActivity.start(context)
        }
    }
}

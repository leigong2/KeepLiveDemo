package zune.keeplivelibrary.app

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES.N
import android.os.PowerManager
import android.provider.Settings
import android.support.v4.app.JobIntentService
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.Utils
import com.xiaomi.mipush.sdk.MiPushClient
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import zune.keeplivelibrary.config.ConstantsConfig
import zune.keeplivelibrary.event.ServiceDeadEvent
import zune.keeplivelibrary.service.BaseOService.Companion.ALARM_INTERVAL
import zune.keeplivelibrary.service.SimpleService
import zune.keeplivelibrary.service.high.AidlOService
import zune.keeplivelibrary.service.high.MainOService
import zune.keeplivelibrary.service.high.RemoteOService
import zune.keeplivelibrary.service.low.AidlService
import zune.keeplivelibrary.service.low.MainService
import zune.keeplivelibrary.service.low.RemoteService

/**
 * Created by leigong2 on 2018-06-07 007.
 */
class KeepLiveHelper {
    private var mContext: Application? = null
    private var hasRegister = false
    private var mClazz: Class<out Service>? = null
    companion object {
        @SuppressLint("StaticFieldLeak")
        var instance: KeepLiveHelper? = null
        var screenOn:Boolean = false
        fun getDefault(): KeepLiveHelper {
            if (instance == null) {
                synchronized(KeepLiveHelper::class) {
                    if (instance == null) {
                        instance = KeepLiveHelper()
                    }
                }
            }
            return instance!!
        }
    }

    fun init(context: Application, PUSH_APP_ID:String, PUSH_APP_KEY:String) {
        mContext = context
        Utils.init(context)
        SPUtils.getInstance().put("PUSH_APP_ID", PUSH_APP_ID)
        SPUtils.getInstance().put("PUSH_APP_KEY", PUSH_APP_KEY)
        initToggle()
        if (ConstantsConfig.nToggle && android.os.Build.VERSION.SDK_INT >= N) {
            MainOService.start(context)
        } else if (android.os.Build.VERSION.SDK_INT < N) {
            MainService.start(context)
        } else {
            //Todo NOTHING
        }

        if (ConstantsConfig.powToggle) {
            if (!isIgnoringBatteryOptimizations(context)) {
                mActivity?.let { gotoSettingIgnoringBatteryOptimizations(it) }
            }
        } else {
            if (isIgnoringBatteryOptimizations(context)) {
                mActivity?.let { gotoSettingIgnoringBatteryOptimizations(it) }
            }
        }

        if (ConstantsConfig.aidlToggle && android.os.Build.VERSION.SDK_INT >= N ) {
            AidlOService.start(context)
        } else if (ConstantsConfig.aidlToggle) {
            AidlService.start(context)
        } else {
            //Todo NOTHING
        }

        if (ConstantsConfig.daemonToggle) {

        } else {

        }

        if (ConstantsConfig.forceToggle) {
            SimpleService.start(context)
        } else {
            //Todo NOTHING
        }

        if (ConstantsConfig.onepointToggle) {

        } else {

        }

        if (ConstantsConfig.remoteToggle) {
            if (ConstantsConfig.nToggle && android.os.Build.VERSION.SDK_INT >= N) {
                RemoteOService.start(context)
            } else if (android.os.Build.VERSION.SDK_INT < N) {
                RemoteService.start(context)
            } else {
                //Todo NOTHING
            }
        } else {
            //Todo NOTHING
        }

        if (ConstantsConfig.whiteToggle) {
            if (mActivity != null && !SPUtils.getInstance().getBoolean("enter")) {
                enterSetting(mActivity as Context)
            } else if(!SPUtils.getInstance().getBoolean("enter")) {
                enterSetting(context)
            }
        } else {
            //Todo NOTHING
        }

        if (ConstantsConfig.pushToggle) {
            MiPushClient.registerPush(context, PUSH_APP_ID, PUSH_APP_KEY)
            MiPushClient.enablePush(context)
            MiPushClient.setAlias(context, "keeplive", null)
        } else {
            //TODO NOTHING
        }
        if (!hasRegister) {
            EventBus.getDefault().register(this)
            hasRegister = true
        }
        if (android.os.Build.VERSION.SDK_INT >= N) {
            KeepLiveHelper.getDefault().startBindOService()
        } else {
            KeepLiveHelper.getDefault().startBindService(mContext)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onScreenOffEvent(event: ServiceDeadEvent) {
        if (ConstantsConfig.nToggle && android.os.Build.VERSION.SDK_INT >= N) {
            MainOService.start(mContext!!, ALARM_INTERVAL)
        } else if (android.os.Build.VERSION.SDK_INT < N) {
            MainService.start(mContext!!)
        } else {
            //Todo NOTHING
        }
    }

    fun onTerminate() {
        EventBus.getDefault().unregister(this)
    }

    fun onActivityForResult(resultCode:Int, requestCode:Int, data:Intent?) {
        ConstantsConfig.powToggle = isIgnoringBatteryOptimizations(mContext)
        saveToggle()
    }

    private var mActivity: Activity? = null

    fun onActivityCreate(activity: Activity) {
        mActivity = activity
    }

    fun onActivityRelease() {
        mActivity = null
    }

    /**是否已经忽略电池优化*/
    fun isIgnoringBatteryOptimizations(context: Context?): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val packageName = context?.packageName
            val pm = context?.getSystemService(Context.POWER_SERVICE) as PowerManager
            return pm.isIgnoringBatteryOptimizations(packageName)
        }
        return false
    }

    /**去设置电池忽略优化**/
    private val REQUEST_IGNORE_BATTERY_CODE = 1001
    private fun gotoSettingIgnoringBatteryOptimizations(context: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent()
                val packageName = context.packageName
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:$packageName")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivityForResult(intent, REQUEST_IGNORE_BATTERY_CODE)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**进入白名单列表上一层级**/
    private fun enterSetting(activity: Context) {
        try {
            val intent = Intent()
            intent.action = "com.android.settings.action.SETTINGS"
            intent.addCategory("com.android.settings.category")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.`package` = "com.android.settings"
            intent.setClassName("com.android.settings"
                    , "com.android.settings.Settings\$PowerUsageSummaryActivity")
            activity.startActivity(intent)
            SPUtils.getInstance().put("enter", true)
        } catch (e:Exception) {
            println("enterSetting e = $e")
        }
    }

    fun initToggle() {
        ConstantsConfig.powToggle = SPUtils.getInstance().getBoolean("power_lay")
        ConstantsConfig.aidlToggle = SPUtils.getInstance().getBoolean("two_process")
        ConstantsConfig.alarmToggle = SPUtils.getInstance().getBoolean("alarm")
        ConstantsConfig.remoteToggle = SPUtils.getInstance().getBoolean("remote")
        ConstantsConfig.whiteToggle = SPUtils.getInstance().getBoolean("white_list")
        ConstantsConfig.musicToggle = SPUtils.getInstance().getBoolean("music")
        ConstantsConfig.onepointToggle = SPUtils.getInstance().getBoolean("one_point")
        ConstantsConfig.forceToggle = SPUtils.getInstance().getBoolean("force_service")
        ConstantsConfig.pushToggle = SPUtils.getInstance().getBoolean("push_service")
        ConstantsConfig.screenToggle = SPUtils.getInstance().getBoolean("screen_service")
        ConstantsConfig.daemonToggle = SPUtils.getInstance().getBoolean("daemon_service")
        ConstantsConfig.nToggle = SPUtils.getInstance().getBoolean("for_n")
    }

    fun saveToggle() {
        SPUtils.getInstance().put("power_lay", ConstantsConfig.powToggle)
        SPUtils.getInstance().put("two_process", ConstantsConfig.aidlToggle)
        SPUtils.getInstance().put("alarm", ConstantsConfig.alarmToggle)
        SPUtils.getInstance().put("remote", ConstantsConfig.remoteToggle)
        SPUtils.getInstance().put("white_list", ConstantsConfig.whiteToggle)
        SPUtils.getInstance().put("music", ConstantsConfig.musicToggle)
        SPUtils.getInstance().put("one_point", ConstantsConfig.onepointToggle)
        SPUtils.getInstance().put("force_service", ConstantsConfig.forceToggle)
        SPUtils.getInstance().put("push_service", ConstantsConfig.pushToggle)
        SPUtils.getInstance().put("screen_service", ConstantsConfig.screenToggle)
        SPUtils.getInstance().put("daemon_service", ConstantsConfig.daemonToggle)
        SPUtils.getInstance().put("for_n", ConstantsConfig.nToggle)
    }

    fun bindService(clazz: Class<out Service>?) {
        this.mClazz = clazz
    }

    fun startBindService(context: Context?) {
        if (mClazz != null) {
            try {
                val intent = Intent(context, mClazz)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context?.startService(intent)
            } catch (e: Exception) {
                println("zune: e$e")
            }
        }
    }

    fun startBindOService() {
        if (mContext != null && mClazz != null) {
            try {
                JobIntentService.enqueueWork(mContext!!, mClazz!!, 10000, Intent())
            } catch (e:Exception) {
                print("zune: e$e")
            }
        }
    }
}
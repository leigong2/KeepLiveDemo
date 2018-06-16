package keeplive.zune.com.keeplivedemo;

import android.content.Context
import android.content.Intent
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES.N
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import zune.keeplivelibrary.app.KeepLiveHelper
import zune.keeplivelibrary.config.ConstantsConfig

class MainActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        KeepLiveHelper.getDefault().initToggle()
        initToggle()
        KeepLiveHelper.getDefault().onActivityCreate(this)
        initListener()
    }

    private fun initToggle() {
        power_toggle.isChecked = ConstantsConfig.powToggle
        process_toggle.isChecked = ConstantsConfig.aidlToggle
        alarm_toggle.isChecked = ConstantsConfig.alarmToggle
        remote_toggle.isChecked = ConstantsConfig.remoteToggle
        white_toggle.isChecked = ConstantsConfig.whiteToggle
        music_toggle.isChecked = ConstantsConfig.musicToggle
        one_point_toggle.isChecked = ConstantsConfig.onepointToggle
        force_toggle.isChecked = ConstantsConfig.forceToggle
        push_toggle.isChecked = ConstantsConfig.pushToggle
        screen_toggle.isChecked = ConstantsConfig.screenToggle
        daemon_toggle.isChecked = ConstantsConfig.daemonToggle
        n_toggle.isChecked = ConstantsConfig.nToggle
    }

    private fun initListener() {
        power_lay.setOnClickListener {
            /** 忽略电池优化**/
            power_toggle.isChecked = !power_toggle.isChecked
        }
        two_process.setOnClickListener {
            /** aidl多进程守护**/
            process_toggle.isChecked = !process_toggle.isChecked
        }
        alarm.setOnClickListener {
            /** alarmManager开关**/
            alarm_toggle.isChecked = !alarm_toggle.isChecked
        }
        remote.setOnClickListener {
            /** 远程服务开关**/
            remote_toggle.isChecked = !remote_toggle.isChecked
        }
        white_list.setOnClickListener {
            /** 白名单开关**/
            white_toggle.isChecked = !white_toggle.isChecked
        }
        music.setOnClickListener {
            /** 循环播放一段空音乐开关**/
            music_toggle.isChecked = !music_toggle.isChecked
        }
        one_point.setOnClickListener {
            /** 一个像素点开关**/
            one_point_toggle.isChecked = !one_point_toggle.isChecked
        }
        force_service.setOnClickListener {
            /** 前台服务开关**/
            force_toggle.isChecked = !force_toggle.isChecked
        }
        push_service.setOnClickListener {
            /** 推送开关**/
            push_toggle.isChecked = !push_toggle.isChecked
        }
        screen_service.setOnClickListener {
            /** 亮屏锁屏开关**/
            screen_toggle.isChecked = !screen_toggle.isChecked
        }
        daemon_service.setOnClickListener {
            /** Daemon通知权限开关**/
            daemon_toggle.isChecked = !daemon_toggle.isChecked
        }
        for_n.setOnClickListener {
            /** android N 适配开关**/
            n_toggle.isChecked = !n_toggle.isChecked
        }
        btn_sure.setOnClickListener {
            saveToggle()
        }
    }

    private fun saveToggle() {
        ConstantsConfig.powToggle = power_toggle.isChecked
        ConstantsConfig.aidlToggle = process_toggle.isChecked
        ConstantsConfig.alarmToggle = alarm_toggle.isChecked
        ConstantsConfig.remoteToggle = remote_toggle.isChecked
        ConstantsConfig.whiteToggle = white_toggle.isChecked
        ConstantsConfig.musicToggle = music_toggle.isChecked
        ConstantsConfig.onepointToggle = one_point_toggle.isChecked
        ConstantsConfig.forceToggle = force_toggle.isChecked
        ConstantsConfig.pushToggle = push_toggle.isChecked
        ConstantsConfig.screenToggle = screen_toggle.isChecked
        ConstantsConfig.daemonToggle = daemon_toggle.isChecked
        ConstantsConfig.nToggle = n_toggle.isChecked
        KeepLiveHelper.getDefault().saveToggle()
        KeepLiveHelper.getDefault().init(MainApp.getApp(), MainApp.getApp().PUSH_APP_ID, MainApp.getApp().PUSH_APP_KEY)
        if (ConstantsConfig.powToggle &&
                !KeepLiveHelper.getDefault().isIgnoringBatteryOptimizations(MainApp.getApp())) {
            return
        }
        onHome()
    }

    private fun onHome() {
        if (VERSION.SDK_INT >= N) {
            finish()
            return
        }
        val intent = Intent()
        intent.action = "android.intent.action.MAIN"
        intent.addCategory("android.intent.category.HOME")
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        KeepLiveHelper.getDefault().onActivityRelease()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        KeepLiveHelper.getDefault().onActivityForResult(resultCode, requestCode, data)
    }
}

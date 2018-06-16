package zune.keeplivelibrary.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION_CODES.N
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import zune.keeplivelibrary.R
import zune.keeplivelibrary.app.KeepLiveHelper
import zune.keeplivelibrary.event.ScreenEvent
import zune.keeplivelibrary.service.high.AidlOService
import zune.keeplivelibrary.service.high.MainOService
import zune.keeplivelibrary.service.high.RemoteOService
import zune.keeplivelibrary.service.low.AidlService
import zune.keeplivelibrary.service.low.MainService
import zune.keeplivelibrary.service.low.RemoteService

/**
 * Created by leigong2 on 2018-06-09 009.
 */
class OnePointActivity:AppCompatActivity(){
    companion object {
        fun start(context:Context){
            val intent = Intent(context, OnePointActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
        fun start(context:Context, onHome:Boolean){
            val intent = Intent(context, OnePointActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("onHome", onHome)
            context.startActivity(intent)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mWindow = window
        mWindow.setGravity(Gravity.LEFT or Gravity.TOP)
        val attrParams = mWindow.attributes
        attrParams.x = 0
        attrParams.y = 0
        attrParams.height = 1
        attrParams.width = 1
        mWindow.attributes = attrParams
        overridePendingTransition(R.anim.alpha_anim, R.anim.alpha_anim)
        EventBus.getDefault().register(this)
        println("zune: onCreate")
        if (intent?.getBooleanExtra("onHome", false)!!) {
            onHome()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (android.os.Build.VERSION.SDK_INT >= N) {
            MainOService.activityStart(this)
            RemoteOService.activityStart(this)
            AidlOService.activityStart(this)
            KeepLiveHelper.getDefault().startBindOService()
        }
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onScreenOnEvent(event: ScreenEvent) {
        println("zune: 收到了开屏时间eventScreenOn = ${event.screenOn}")
        if (event.screenOn) {
            onHome()
        }
    }

    private fun onHome() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            finish()
            return
        }
        val intent = Intent()
        intent.action = "android.intent.action.MAIN"
        intent.addCategory("android.intent.category.HOME")
        startActivity(intent)
        if (android.os.Build.VERSION.SDK_INT < N) {
            MainService.start(this)
            RemoteService.start(this)
            AidlService.start(this)
            KeepLiveHelper.getDefault().startBindService(this)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        println("zunea: onNewIntent  =  ${intent?.getBooleanExtra("onHome", false)}" )
        if (intent != null && intent.getBooleanExtra("onHome", false)) {
            onHome()
        }
    }
}
package zune.keeplivelibrary.listener
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log

/**
 * Created by 王志龙 on 2017/11/29 029.
 */

class ScreenBroadcastListener private constructor(context: Context) {
    private var mContext: Context = context.applicationContext

    private var mScreenReceiver: ScreenBroadcastReceiver

    private var mListener: ScreenStateListener? = null

    init {
        mScreenReceiver = ScreenBroadcastReceiver()
    }

    interface ScreenStateListener {

        fun onScreenOn()

        fun onScreenOff()
    }

    /**
     * screen状态广播接收者
     */
    private inner class ScreenBroadcastReceiver : BroadcastReceiver() {
        private var action: String? = null

        override fun onReceive(context: Context, intent: Intent) {
            action = intent.action
            if (Intent.ACTION_SCREEN_ON == action) {
                mListener!!.onScreenOn()
                Log.i("zune", "开屏 mListener = ${mListener == null}")
            } else if (Intent.ACTION_SCREEN_OFF == action) {
                Log.i("zune", "锁屏 mListener = ${mListener == null}")
                mListener!!.onScreenOff()
            }
        }
    }

    fun registerListener(listener: ScreenStateListener) {
        mListener = listener
        registerListener()
    }

    private fun registerListener() {
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        mContext.registerReceiver(mScreenReceiver, filter)
    }

    companion object {

        @SuppressLint("StaticFieldLeak")
        private var listener: ScreenBroadcastListener? = null

        fun getInstance(context: Context): ScreenBroadcastListener {
            if (listener == null)
                listener = ScreenBroadcastListener(context)
            return listener as ScreenBroadcastListener
        }
    }
}

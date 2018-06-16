package zune.keeplivelibrary.util

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.SystemClock

/**
 * Created by leigong2 on 2018-04-05 005.
 */

class AlarmSendUtil {

    @SuppressLint("NewApi")
    fun sendCallLiveBroadcast(context: Context, Time: Long) {
        val mIntent = Intent("zune.keeplivelibrary.receiver.CallLiveBroadCastReceiver")
        // 发送广播

        // 和Handler定时广播不同这里只执行一次，执行多次的是接受到广播消息，所以这里没用
        // mIntent.putExtra(MESSAGE,
        // "第"+countAlarm+"次"+"AlarmManager方式发送过来的广播,  是时候表演真正的第"+countAlarm+"次技术了");
        // AlarmManager方式发送广播
        context.sendBroadcast(mIntent)
        // 触发服务的起始时间 这里是// 5秒后发送广播，只发送一次

        val pendIntent = PendingIntent.getBroadcast(context, 0,
                mIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        // 进行闹铃注册
        val manager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        // 每隔5秒重复发广播
        manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(), Time, pendIntent)
    }

    companion object {
        private var util: AlarmSendUtil? = null
        val instanse: AlarmSendUtil
            get() {
                if (util == null)
                    util = AlarmSendUtil()
                return util!!
            }
    }
}

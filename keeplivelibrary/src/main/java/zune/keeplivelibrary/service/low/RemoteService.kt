package zune.keeplivelibrary.service.low

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.zune.test180311.util.AlarmSendUtil
import zune.keeplivelibrary.service.BaseService

/**
 * Created by leigong2 on 2018-06-07 007.
 */
class RemoteService: BaseService() {
    var conn: ServiceConnection? = null
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, RemoteService::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startService(intent)
        }
        fun bind(context: Context, conn: ServiceConnection) {
            context.bindService(Intent(context, RemoteService::class.java), conn, Context.BIND_IMPORTANT)
        }
    }
    override fun onCreate() {
        super.onCreate()
        if (conn == null) {
            conn = Myconn()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            MainService.bind(applicationContext, this@RemoteService.conn!!)
        } catch (e: Exception) {
            Log.i("zune: ", "不允许后台启动 e = $e")
        }
        return super.onStartCommand(intent, flags, startId)
    }
    internal inner class Myconn : ServiceConnection {
        override fun onServiceDisconnected(componentName: ComponentName) {
            Log.i("zune:", "远程服务进程被杀死")
            AlarmSendUtil.instanse.sendCallLiveBroadcast(applicationContext, 0)
            try {
                MainService.start(applicationContext)
                MainService.bind(applicationContext, this@RemoteService.conn!!)
            } catch (e: Exception) {
                Log.i("zune: ", "不允许后台启动 e = $e")
            }
        }

        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            Log.i("zune:", "连接远程服务进程成功")
        }
    }
}
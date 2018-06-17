package zune.keeplivelibrary.util

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by leigong2 on 2018-03-31 031.
 */
class CrashHandler
/** 保证只有一个CrashHandler实例  */
private constructor() : Thread.UncaughtExceptionHandler {

    // 系统默认的UncaughtException处理类
    private var mDefaultHandler: Thread.UncaughtExceptionHandler? = null
    // 程序的Context对象
    private var mContext: Context? = null
    // 用来存储设备信息和异常信息
    private val infos = HashMap<String, String>()

    // 用于格式化日期,作为日志文件名的一部分
    private val formatter = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")

    /**
     * 初始化
     *
     * @param context
     */
    fun init(context: Context) {
        mContext = context
        // 获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        // 设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    override fun uncaughtException(thread: Thread, ex: Throwable) {
        if (!handleException(ex) && mDefaultHandler != null) {
            // 如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler!!.uncaughtException(thread, ex)
        }
        //退出程序
        android.os.Process.killProcess(android.os.Process.myPid())
        System.exit(1)
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private fun handleException(ex: Throwable?): Boolean {
        if (ex == null) {
            return false
        }
        // 保存日志文件
        val fileName = saveCrashInfo2File(ex)
        Log.i("zune: ", "保存日志 = " + fileName!!)
        return true
    }

    /**
     * 保存错误信息到文件中
     *
     * @param ex
     * @return 返回文件名称,便于将文件传送到服务器
     */
    private fun saveCrashInfo2File(ex: Throwable): String? {

        val sb = StringBuffer()
        for ((key, value) in infos) {
            sb.append("$key=$value\n")
        }

        val writer = StringWriter()
        val printWriter = PrintWriter(writer)
        ex.printStackTrace(printWriter)
        var cause: Throwable? = ex.cause
        Log.i("zune: ", "cause = " + cause!!.message)
        while (cause != null) {
            cause.printStackTrace(printWriter)
            cause = cause.cause
        }
        printWriter.close()
        val result = writer.toString()
        sb.append(result)
        try {
            val timestamp = System.currentTimeMillis()
            val time = formatter.format(Date(timestamp))
            val fileName = "bing-$time.txt"
            val dir = File(mContext!!.externalCacheDir, fileName)
            val fos = FileOutputStream(dir)
            fos.write(sb.toString().toByteArray())
            fos.close()
            return fileName
        } catch (e: Exception) {
            Log.e(TAG, "an error occured while writing file...", e)
        }

        return null
    }

    companion object {
        val TAG = "zune: "
        // CrashHandler实例
        /** 获取CrashHandler实例 ,单例模式  */
        @SuppressLint("StaticFieldLeak")
        val instance = CrashHandler()

        fun isAppaLive(context: Context, packageName: String): Boolean {
            val am = context
                    .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val list = am.getRunningTasks(100)
            var isAppRunning = false
            //String MY_PKG_NAME = "你的包名";
            for (info in list) {
                if (info.topActivity.packageName == packageName//如果想要手动输入的话可以str换成<span style="font-family: Arial, Helvetica, sans-serif;">MY_PKG_NAME，下面相同</span>
                        || info.baseActivity.packageName == packageName) {
                    isAppRunning = true
                    break
                }
            }
            return isAppRunning
        }

        /**
         * 判断服务是否开启
         *
         * @return
         */
        fun isServiceRunning(context: Context, ServiceName: String?): Boolean {
            if ("" == ServiceName || ServiceName == null)
                return false
            val myManager = context
                    .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val runningService = myManager
                    .getRunningServices(30) as ArrayList<ActivityManager.RunningServiceInfo>
            for (i in runningService.indices) {
                if (runningService[i].service.className.toString() == ServiceName) {
                    return true
                }
            }
            return false
        }
    }
}


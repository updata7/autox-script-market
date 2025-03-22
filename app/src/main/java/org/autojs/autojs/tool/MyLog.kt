package org.autojs.autojs.tool

import android.util.Log

open class MyLog {
    companion object {
        private const val TAG: String = "AgentStoreX"
        @JvmStatic
        fun d(msg: String) {
            Log.d(TAG, msg)
        }

        @JvmStatic
        fun d(tag: String, msg: String) {
            Log.d(tag, msg)
        }

        @JvmStatic
        fun e(msg: String) {
            Log.e(TAG, msg)
        }

        @JvmStatic
        fun e(tag: String, msg: String) {
            Log.e(tag, msg)
        }

        @JvmStatic
        fun printTrace() {
            // 打印调用堆栈
            val stackTrace = Throwable().stackTrace
            d("stackTrace start =========================> ")
            stackTrace.forEach { element ->
                d("${element.className}.${element.methodName}(${element.fileName}:${element.lineNumber})")
            }
            d("stackTrace end ============================> ")
        }
    }
}
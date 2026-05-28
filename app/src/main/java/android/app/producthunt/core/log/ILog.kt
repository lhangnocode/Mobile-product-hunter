package android.app.producthunt.core.log

import android.app.producthunt.BuildConfig
import android.util.Log

object ILog {
    private const val APP_TAG = "LOG"

    fun d(tag: String, vararg messages: Any?) {
        if (BuildConfig.DEBUG) {
            Log.d(APP_TAG, formatMessage(tag, messages))
        }
    }

    fun i(tag: String, vararg messages: Any?) {
        Log.i(APP_TAG, formatMessage(tag, messages))
    }

    fun w(tag: String, vararg messages: Any?, throwable: Throwable? = null) {
        Log.w(APP_TAG, formatMessage(tag, messages), throwable)
    }

    fun e(tag: String, vararg messages: Any?, throwable: Throwable? = null) {
        Log.e(APP_TAG, formatMessage(tag, messages), throwable)
    }

    private fun formatMessage(tag: String, messages: Array<out Any?>): String =
        "[$tag] ${messages.joinToString(" ")}"
}

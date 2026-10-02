package com.ce46.connectme.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActivityLogStore(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    fun restore(): List<LogLine> = decode(prefs.getString(KEY, "[]"))

    fun append(message: String): List<LogLine> {
        val line = LogLine(timeFormat.format(Date()), message)
        val next = (restore() + line).takeLast(80)
        prefs.edit().putString(KEY, encode(next)).apply()
        return next
    }

    fun clear() {
        prefs.edit().remove(KEY).apply()
    }

    private fun encode(lines: List<LogLine>): String {
        val arr = JSONArray()
        lines.forEach { line ->
            arr.put(JSONObject().put("t", line.time).put("m", line.message))
        }
        return arr.toString()
    }

    private fun decode(raw: String?): List<LogLine> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    add(LogLine(obj.optString("t"), obj.optString("m")))
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    companion object {
        private const val PREFS = "connectme_log"
        private const val KEY = "lines"
    }
}

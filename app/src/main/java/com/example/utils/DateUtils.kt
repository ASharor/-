package com.example.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {

    // Target Konkur Date: 1405/04/12 (~ July 3, 2026)
    private val konkurTargetMillis: Long by lazy {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tehran"))
        cal.set(2026, Calendar.JULY, 3, 8, 0, 0)
        cal.timeInMillis
    }

    data class CountdownTime(val days: Long, val hours: Long, val minutes: Long, val seconds: Long)

    fun getKonkurCountdown(): CountdownTime {
        val diff = konkurTargetMillis - System.currentTimeMillis()
        if (diff <= 0) return CountdownTime(0, 0, 0, 0)

        val seconds = (diff / 1000) % 60
        val minutes = (diff / (1000 * 60)) % 60
        val hours = (diff / (1000 * 60 * 60)) % 24
        val days = diff / (1000 * 60 * 60 * 24)

        return CountdownTime(days, hours, minutes, seconds)
    }

    fun getCurrentIsoDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    fun formatDuration(minutes: Int, isPersian: Boolean = true): String {
        val h = minutes / 60
        val m = minutes % 60
        return if (isPersian) {
            if (h > 0) "$h ساعت $m دقیقه" else "$m دقیقه"
        } else {
            if (h > 0) "${h}h ${m}m" else "${m}m"
        }
    }

    fun formatSeconds(totalSeconds: Long): String {
        val hours = totalSeconds / 3600
        val mins = (totalSeconds % 3600) / 60
        val secs = totalSeconds % 60
        return if (hours > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", hours, mins, secs)
        } else {
            String.format(Locale.US, "%02d:%02d", mins, secs)
        }
    }

    fun toPersianDate(dateMillis: Long): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = dateMillis
        val gy = cal.get(Calendar.YEAR)
        val gm = cal.get(Calendar.MONTH) + 1
        val gd = cal.get(Calendar.DAY_OF_MONTH)

        val jalali = gregorianToJalali(gy, gm, gd)
        return "${jalali[0]}/${"%02d".format(jalali[1])}/${"%02d".format(jalali[2])}"
    }

    private fun gregorianToJalali(gy: Int, gm: Int, gd: Int): IntArray {
        val gDaysInMonth = intArrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val jDaysInMonth = intArrayOf(0, 31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

        var gy2 = if (gm > 2) gy + 1 else gy
        var days = 365 * gy + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400 - 80 + gd
        for (i in 0 until gm) {
            days += gDaysInMonth[i]
        }
        if (gm > 2 && (gy % 4 == 0 && gy % 100 != 0 || gy % 400 == 0)) {
            days++
        }

        var jy = -1595 + 33 * (days / 12053)
        days %= 12053

        jy += 4 * (days / 1461)
        days %= 1461

        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }

        var jm = 0
        var jd = 0
        val isJalaliLeap = (jy % 33 == 1 || jy % 33 == 5 || jy % 33 == 9 || jy % 33 == 13 || jy % 33 == 17 || jy % 33 == 22 || jy % 33 == 26 || jy % 33 == 30)
        if (isJalaliLeap) jDaysInMonth[12] = 30

        for (i in 1..12) {
            if (days < jDaysInMonth[i]) {
                jm = i
                jd = days + 1
                break
            }
            days -= jDaysInMonth[i]
        }

        return intArrayOf(jy, jm, jd)
    }
}

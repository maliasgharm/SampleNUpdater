package com.noandish.nupdate.utils

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.noandish.nupdate.BuildConfig
import java.io.File
import java.text.DecimalFormat

object Utils {
    const val DEFAULT_URL_UPDATE = "http://148.251.100.207/nUpdater/"
    val screenWidth: Int
        get() = Resources.getSystem().displayMetrics.widthPixels


    val screenHeight: Int
        get() = Resources.getSystem().displayMetrics.heightPixels

    fun installApk(context: Context, pathApk: String): Boolean {
        if (!File(pathApk).exists()) {
            Log.w("Utils", "file not exist $pathApk")
            Toast.makeText(context, "فایل وجود ندارد !", Toast.LENGTH_LONG).show()
            return false
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val uri = FileProvider.getUriForFile(
                context,
                BuildConfig.APPLICATION_ID + ".provider",
                File(pathApk)
            )
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "application/vnd.android.package-archive")
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.startActivity(intent)
        } else {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(
                Uri.fromFile(File(pathApk)),
                "application/vnd.android.package-archive"
            )
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }


        return true
    }

    private val K: Long = 1024
    private val M = K * K
    private val G = M * K
    private val T = G * K
    fun convertToStringRepresentation(value: Long): String? {
        val dividers = longArrayOf(T, G, M, K, 1)
        val units = arrayOf("ترابایت", "گیگابایت", "مگابایت", "کیلو بایت", "بایت")
        if (value < 1)
            return "0"
        var result: String? = null
        for (i in dividers.indices) {
            val divider = dividers[i]
            if (value >= divider) {
                result = format(value, divider, units[i])
                break
            }
        }
        return result
    }

    private fun format(
        value: Long,
        divider: Long,
        unit: String
    ): String {
        val result = if (divider > 1) value.toDouble() / divider.toDouble() else value.toDouble()
        return DecimalFormat("#,##0.#").format(result) + " " + unit
    }


}
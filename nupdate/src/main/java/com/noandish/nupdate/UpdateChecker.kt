package com.noandish.nupdate

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Build
import android.os.Handler
import com.noandish.nupdate.connection.Callback
import com.noandish.nupdate.connection.Connection
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.ProtocolException
import java.net.URL

class UpdateChecker(val context: Context) {
    var isUpdated: Boolean? = null
        private set
    var path: String? = null
        private set


    fun check(callback: ((isUpdated: Boolean?, description: String) -> Unit)? = null) {
        val params = JSONObject()
        params.put("sdk", Build.VERSION.SDK_INT)
        params.put("package_name", context.packageName)
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val version = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode
            } else {
                pInfo.versionCode
            }
            params.put("version_code", version)
        } catch (e: PackageManager.NameNotFoundException) {
            params.put("version_code", 0)
            e.printStackTrace()
        }

        Updater.connection?.addRequest(params, "android", countTryToConnect = 10) {
            if (it.type == Callback.TYPE_SUCCESS) {
                val jsonObject = JSONArray(it.response).getJSONObject(0)
                val message = if (jsonObject.has("msg")) jsonObject.getString("msg") else ""
                if (jsonObject.has("res") && jsonObject.getInt("res") == 1) {
                    val description = jsonObject.getString("desc")
                    val path = jsonObject.getString("path")
                    this.path = path
                    callback?.invoke(true, description)
                } else {
                    callback?.invoke(false, message)
                }
            } else {
                callback?.invoke(null, "خطا در دریافت اطلاعات")
            }
        }
    }

    private var callbackProgress: ((progress: Int) -> Unit)? = null
    private var callbackSizeFile: ((progress: Long) -> Unit)? = null
    private var downloadListener: ((progress: TypeDownload, path: String) -> Unit)? = null
    private var cancelListener: (() -> Unit)? = null
    fun update(
        callbackSizeFile: (size: Long) -> Unit,
        callback: (progress: Int) -> Unit,
        downloadListener: ((progress: TypeDownload, path: String) -> Unit)
    ) {
        this.callbackProgress = callback
        this.callbackSizeFile = callbackSizeFile
        this.downloadListener = downloadListener
        DownloadUpdate().execute(path)
    }

    fun cancelDownload() {
        cancelListener?.invoke()
    }

    enum class TypeDownload {
        DOWNLOAD_COMPLETE, START_DOWNLOAD
    }

    @SuppressLint("StaticFieldLeak")
    private inner class DownloadUpdate : AsyncTask<String, Int, String>() {
        var size = 0
        lateinit var handler: Handler
        override fun onPreExecute() {
            handler = Handler()
            super.onPreExecute()
        }

        @SuppressLint("WrongThread")
        override fun doInBackground(vararg strings: String): String {
            val name = strings[0].substring(strings[0].lastIndexOf('/') + 1, strings[0].length)
            val path = File(context.externalCacheDir!!.toString() + "/update")
            if (!path.exists())
                path.mkdirs()

            val directory = File(path.toString() + "/" + name)
            handler.post {
                downloadListener?.invoke(TypeDownload.START_DOWNLOAD, directory.toString())
                callbackProgress?.invoke(0)
            }
            try {
                val f = FileOutputStream(directory)
                val u = URL(strings[0])
                val c = u.openConnection() as HttpURLConnection
                c.doOutput = true
                c.connect()
                val `in` = c.inputStream
                val length = c.contentLength
                size = length
                handler.post {
                    callbackSizeFile?.invoke(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) c.contentLengthLong else c.contentLength.toLong()
                    )

                }
                val buffer = ByteArray(1024)
                var bytesRead = 0
                var sentData: Long = 0
                bytesRead = `in`.read(buffer)
                cancelListener = {
                    f.close()
                }
                while (bytesRead > 0) {
                    f.write(buffer, 0, bytesRead)
                    sentData += bytesRead.toLong()
                    val progress = (sentData).toFloat()
                    handler.post {
                        callbackProgress?.invoke(if (size == 0) 0 else ((progress / size.toFloat()) * 1000L).toInt())
                    }
                    try {

                        bytesRead = `in`.read(buffer)
                    } catch (e: ProtocolException) {
                        bytesRead = 0
                        e.printStackTrace()
                    }
                }
                f.close()
//                handler.post {
//                    downloadListener?.invoke(TypeDownload.DOWNLOAD_COMPLETE, directory.toString())
//                }
                return directory.toString()
            } catch (e: Exception) {
                e.printStackTrace()
                return ""
            }

        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            handler.post {
                downloadListener?.invoke(TypeDownload.DOWNLOAD_COMPLETE, result!!.toString())
            }
        }
    }

    companion object {
        private const val TAG = "UpdateChecker"
    }
}
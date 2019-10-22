package com.noandish.nupdate.connection

import android.content.Context
import android.os.Handler
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.noandish.nupdate.utils.DebugUtils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.Reader
import java.net.URL
import java.nio.charset.Charset
import java.util.HashMap

class Connection(val context: Context) {
    var queue: RequestQueue = Volley.newRequestQueue(context)
    var urlMain: String? = null
    private val listLogOutListener = ArrayList<(() -> Unit)>()
    /**
     *[timeForTry] Can not be less than 100ms
     */
    var timeForTry = 1000L
        set(value) {
            if (value < 100)
                throw ConnectionException("Can not be less than 100ms")

            field = value
        }

    /**
     *
     * [countTryToConnect] [TRY_UNLIMITED] is unlimited try for Connect and null is one try to Connect
     */
    @Throws(ConnectionException::class)
    fun addRequest(
        jsonParamsPost: JSONObject = JSONObject(),
        mClass: String? = null,
        url: String? = null,
        countTryToConnect: Int? = null,
        typeSend: Int = 1
        , mCallback: (callback: Callback) -> Unit
    ) {
        var request: StringRequest? = null
        var countTry = 0
        val url = if (url == null && mClass != null && urlMain != null) "$urlMain/$mClass"
        else if (url != null && mClass != null) "$url/$mClass"
        else if (url != null && mClass == null) "$url"
        else if (url == null && mClass == null && urlMain != null) urlMain
        else {
            throw ConnectionException("All parameter url is null set url or urlMain")
        }
        Log.w(TAG, "sendParams : url :$url post : $jsonParamsPost")

        DebugUtils(
            context,
            "Connection",
            "sendParams : url :$url post : $jsonParamsPost"
        )
        jsonParamsPost.put("token", getLastToken())
        request = object : StringRequest(
            Method.POST, url,
            Response.Listener<String> { response ->

                Log.w(
                    TAG,
                    "receivedParams for class $mClass ,with params :$jsonParamsPost ,response: $response"
                )

                DebugUtils(
                    context,
                    "Connection",
                    "receivedParams for class $mClass ,with params :$jsonParamsPost ,response: $response"
                )
                val callback = Callback(Callback.TYPE_SUCCESS, response)
                try {
                    val jsonObject = JSONArray(response.toString()).getJSONObject(0)
                    if (jsonObject.has("res") && jsonObject.getInt("res") == -1) {
                        listLogOutListener.forEach {
                            it.invoke()
                        }
                    }
                    if (jsonObject.has("token")) {
                        setToken(jsonObject.getString("token"))
                    }
                } catch (e: JSONException) {
//                    e.printStackTrace()
                }
                try {
                    val jsonObject = JSONObject(response.toString())
                    if (jsonObject.has("res") && jsonObject.getInt("res") == -1) {
                        listLogOutListener.forEach {
                            it.invoke()
                        }
                        return@Listener
                    }
                    if (jsonObject.has("token")) {
                        setToken(jsonObject.getString("token"))
                    }
                } catch (e: JSONException) {
//                    e.printStackTrace()
                }
                callback.setListenerTryAgain { queue.add(request) }

                mCallback.invoke(callback)
            },
            Response.ErrorListener {
                if (countTryToConnect == null || (countTryToConnect != TRY_UNLIMITED && countTry >= countTryToConnect)) {
                    Log.w(TAG, "count try : $countTry")
                    val callback = Callback(
                        Callback.TYPE_FAILURE,
                        "Error Connection : ${it.message ?: "not found message"}"
                    )

                    DebugUtils(
                        context,
                        "Connection",
                        "Error Connection : ${it.message
                            ?: "not found message"}  for url :$url and post params :$jsonParamsPost "
                    )

                    callback.setListenerTryAgain {
                        queue.add(request)
                    }

                    mCallback.invoke(callback)
                } else {
                    Handler().postDelayed({
                        queue.add(request)
                        countTry++
                    }, timeForTry)
                }
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json; charset=UTF-8"
                return super.getHeaders()
            }

            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                Log.i(TAG, "url : $url & sendingParams : $jsonParamsPost")
                return jsonParamsPost.toString().toByteArray(Charsets.UTF_8)
            }
//            @Throws(AuthFailureError::class)
//            override fun getParams(): Map<String, String> {
//                val params = HashMap<String, String>()
//                params["myData"] = jsonParamsPost.toString()
//                return params
//            }
        }

        countTry++
        queue.add(request)

        request.retryPolicy = DefaultRetryPolicy(
            5000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        queue.start()
    }

    fun addOnLogoutListener(listener: () -> Unit) {
        this.listLogOutListener.add(listener)
    }

    private fun getLastToken(): String? {
        return context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE).getString(
            KEY_TOKEN, ""
        )
    }

    private fun setToken(token: String) {
        context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE).edit().putString(
            KEY_TOKEN, token
        )
            .apply()
    }

    fun clearToken() {
        context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE).edit().putString(
            KEY_TOKEN, ""
        ).apply()
    }

    fun addRequest(url: String, mCallback: (callback: Callback) -> Unit) {
        val handler = Handler()
        Thread {
            try {
                val result = get(url)
                handler.post {
                    mCallback.invoke(
                        Callback(
                            if (result == null) Callback.TYPE_FAILURE else Callback.TYPE_SUCCESS,
                            result.toString()
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                handler.post {
                    mCallback.invoke(Callback(Callback.TYPE_FAILURE, ""))
                }
            }
        }.run()

    }

    private operator fun get(sUrl: String): JSONObject? {
        Log.w(TAG, "sUrl :$sUrl")
        val inputStream = URL(sUrl).openStream()
        try {
            val rd =
                BufferedReader(InputStreamReader(inputStream, Charset.forName("UTF-8")) as Reader?)
            val jsonText = convertInputStreamToString(rd)
            val json = JSONObject(jsonText)
            return json
        } finally {
            inputStream.close()
        }


    }

    @Throws(IOException::class)
    private fun convertInputStreamToString(bufferedReader: BufferedReader): String {
//        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        var line: String?
        var result = ""
        line = bufferedReader.readLine()
        while (line != null) {
            line = bufferedReader.readLine()
            result += line
        }

//        inputStream.close()
        return result
    }

    fun getToken(): String? = getLastToken()

    class ConnectionException(override val message: String) :
        Exception("ConnectionException -> $message")


    companion object {
        private const val TAG = "Connection"
        private const val KEY_TOKEN = "key_token"

        const val TRY_UNLIMITED = -1

    }
}
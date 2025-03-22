package org.autojs.autojs.net

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.autojs.autojs.tool.MyLog
import org.autojs.autoxjs.BuildConfig
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class HttpClient {
    companion object {
        private const val TAG = "scriptstoreX_Download"
        private val client = OkHttpClient()
        private val baseUrl: String = BuildConfig.BASE_URL // "https://aphonetest.double.lease"

        @JvmStatic
        @Throws(IOException::class)
        fun get(url: String, headers: Map<String, String> = emptyMap()): String? {
            val requestBuilder = Request.Builder().url(baseUrl + url)

            for ((key, value) in headers) {
                requestBuilder.addHeader(key, value)
            }

            val request = requestBuilder.build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")
                return response.body?.string()
            }
        }

        @JvmStatic
        @Throws(IOException::class)
        fun post(url: String, headers: Map<String, String> = emptyMap(), json: String): Result<String> {
            Log.i("HttpPost", baseUrl + url)
            val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
            val requestBuilder = Request.Builder()
                .url(baseUrl + url)
                .post(body)

            for ((key, value) in headers) {
                requestBuilder.addHeader(key, value)
            }

            val request = requestBuilder.build()
            return try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Result.failure(IOException("Unexpected code $response"))
                    } else {
                        response.body?.string()?.let { Result.success(it) }
                            ?: Result.failure(IOException("Response body is null"))
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        fun download(url: String, savePath: String): Boolean {
            val url2 = URL(url)
            val downloadPath = File(savePath)
            try {
                val parentDir = downloadPath.parentFile
                if (parentDir != null && !parentDir.exists()) {
                    if (parentDir.mkdirs()) {
                        MyLog.d(TAG, "Directory created: ${parentDir.absolutePath}")
                    } else {
                        MyLog.e(TAG, "Failed to create directory: ${parentDir.absolutePath}")
                    }
                }
                val connection = url2.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.doInput = true
                connection.connect()

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream: InputStream = connection.inputStream
                    val outputStream = FileOutputStream(downloadPath)
                    inputStream.copyTo(outputStream)
                    outputStream.close()
                    inputStream.close()
                    MyLog.d(TAG, "File downloaded successfully: ${downloadPath.absolutePath}")
                } else {
                    MyLog.e(TAG, "Failed to download file: ${connection.responseMessage}")
                }
            } catch (e: Exception) {
                MyLog.e(TAG, "Error downloading file: $e")
                return false
            }

            return true
        }
    }
}
package pl.dakil.krotapp.data

import pl.dakil.krotapp.BuildConfig
import android.content.Context
import android.util.Log
import okhttp3.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.IOException
import java.time.LocalDate

object XlsxFileHandler {
    private const val FILE_NAME = "daily_data.xlsx"
    private const val PREF_NAME = "xlsx_prefs"
    private const val KEY_LAST_DATE = "last_download_date"
    private const val TAG = "XLSX"

    enum class Status {
        DOWNLOADING, UP_TO_DATE, SUCCESS, FAILED
    }

    fun maybeDownloadXlsx(context: Context, onStatus: (Status) -> Unit) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val lastDate = prefs.getString(KEY_LAST_DATE, null)
        val today = LocalDate.now().toString()

        if (lastDate == today) {
            Log.d(TAG, "Already downloaded today.")
            onStatus(Status.UP_TO_DATE)
            return
        }

        onStatus(Status.DOWNLOADING)
        downloadXlsx(context) { success ->
            if (success) {
                prefs.edit().putString(KEY_LAST_DATE, today).apply()
                onStatus(Status.SUCCESS)
            } else {
                onStatus(Status.FAILED)
            }
        }
    }

    fun downloadXlsx(context: Context, onResult: (Boolean) -> Unit) {
        val fileId = BuildConfig.SHEET_ID
        val apiKey = BuildConfig.API_KEY
        val url = "https://www.googleapis.com/drive/v3/files/$fileId?alt=media&key=$apiKey"

        val file = File(context.filesDir, FILE_NAME)
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Download failed", e)
                onResult(false)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.e(TAG, "Download failed with code: ${response.code}")
                    onResult(false)
                    return
                }

                try {
                    response.body?.byteStream()?.use { input ->
                        file.outputStream().use { output -> input.copyTo(output) }
                    }
                    Log.d(TAG, "XLSX downloaded and saved.")
                    onResult(true)
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving XLSX file", e)
                    onResult(false)
                }
            }
        })
    }

    private fun getSavedFile(context: Context): File? {
        val file = File(context.filesDir, FILE_NAME)
        return if (file.exists()) file else null
    }
}

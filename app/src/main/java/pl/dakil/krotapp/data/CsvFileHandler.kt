package pl.dakil.krotapp.data

import pl.dakil.krotapp.BuildConfig
import android.content.Context
import android.util.Log
import okhttp3.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.time.LocalDate

object CsvFileHandler {
    private const val FILE_NAME = "daily_data.csv"
    private const val PREF_NAME = "csv_prefs"
    private const val KEY_LAST_DATE = "last_download_date"
    private const val TAG = "CSV"

    enum class Status {
        DOWNLOADING, UP_TO_DATE, SUCCESS, FAILED
    }

    fun maybeDownloadCsv(context: Context, onStatus: (Status) -> Unit) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val lastDate = prefs.getString(KEY_LAST_DATE, null)
        val today = LocalDate.now().toString()

        if (lastDate == today) {
            Log.d(TAG, "Already downloaded today.")
            onStatus(Status.UP_TO_DATE)
            return
        }

        onStatus(Status.DOWNLOADING)
        downloadCsv(context) { success ->
            if (success) {
                prefs.edit().putString(KEY_LAST_DATE, today).apply()
                onStatus(Status.SUCCESS)
            } else {
                onStatus(Status.FAILED)
            }
        }
    }

    fun downloadCsv(context: Context, onResult: (Boolean) -> Unit) {
        val fileId = BuildConfig.SHEET_ID
        val apiKey = BuildConfig.API_KEY
        val url = "https://docs.google.com/spreadsheets/d/$fileId/export?format=tsv&key=$apiKey"

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

    fun parseCsvFile(context: Context): List<Storage> {
        val file = getSavedFile(context) ?: return emptyList()
        val storages = mutableListOf<Storage>()

        val reader = BufferedReader(InputStreamReader(FileInputStream(file)))
        var currentMG: String? = null
        var currentItems = mutableListOf<Item>()

        reader.forEachLine { line ->
            val trimmed = line.trim()
            if (trimmed.isBlank()) return@forEachLine

            val parts = trimmed.split("\t")
            if (parts[0].startsWith("MG:")) {
                if (currentMG != null && currentItems.isNotEmpty()) {
                    storages.add(Storage(currentMG!!, currentItems))
                }
                currentMG = parts[1].trim()
                currentItems = mutableListOf()
            } else if (parts.size >= 4 && parts[0].isNotBlank()) {
                val index = parts[0].trim()
                val name = parts[1].trim()
                val unit = parts[2].trim()
                val amount = parts[3].replace(",", ".").toDoubleOrNull() ?: 0.0

                currentItems.add(Item(index, name, unit, amount))
            }
        }

        if (currentMG != null && currentItems.isNotEmpty()) {
            storages.add(Storage(currentMG!!, currentItems))
        }

        return storages
    }
}

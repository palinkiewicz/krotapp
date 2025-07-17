package pl.dakil.krotapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

class UpdateCheckerViewModel() : ViewModel() {
    enum class Status {
        CURRENT,
        UPDATE
    }

    data class UpdateResult(val status: Status, val version: String? = null, val link: String? = null)

    private val _updateResult = MutableStateFlow(UpdateResult(Status.CURRENT))
    val updateResult: StateFlow<UpdateResult> = _updateResult

    private val versionFileUrl =
        "https://raw.githubusercontent.com/palinkiewicz/krotapp/refs/heads/main/version.data"

    fun checkVersion(currentVersion: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val connection = URL(versionFileUrl).openConnection() as HttpURLConnection
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val lines = connection.inputStream.bufferedReader().readLines()
                if (lines.size < 2) {
                    _updateResult.value = UpdateResult(Status.CURRENT)
                    return@launch
                }

                val remoteVersion = lines[0].trim()
                val downloadLink = lines[1].trim()

                _updateResult.value = if (remoteVersion == currentVersion) {
                    UpdateResult(Status.CURRENT)
                } else {
                    UpdateResult(Status.UPDATE, remoteVersion, downloadLink)
                }

            } catch (e: Exception) {
                _updateResult.value = UpdateResult(Status.CURRENT)
            }
        }
    }
}

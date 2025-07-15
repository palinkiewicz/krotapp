package pl.dakil.krotapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pl.dakil.krotapp.data.CsvFileHandler
import pl.dakil.krotapp.data.Item
import pl.dakil.krotapp.data.Storage

class ItemsViewModel : ViewModel() {
    private val _storages = MutableStateFlow<List<Storage>>(emptyList())
    val storages: StateFlow<List<Storage>> = _storages

    private val _filteredItems = MutableStateFlow<List<Item>>(emptyList())
    val filteredItems: StateFlow<List<Item>> = _filteredItems

    private val _downloadStatus = MutableStateFlow(CsvFileHandler.Status.UP_TO_DATE)
    val downloadStatus: StateFlow<CsvFileHandler.Status> = _downloadStatus

    fun loadData(context: Context) {
        viewModelScope.launch {
            CsvFileHandler.maybeDownloadCsv(context) { status ->
                _downloadStatus.value = status
            }

            val storages: List<Storage> = CsvFileHandler.parseCsvFile(context)
            val items = storages.flatMap { it.items }
            _storages.value = storages
            _filteredItems.value = items
        }
    }

    fun filterItems(mg: String?, name: String?) {
        val queryMG = mg?.trim()?.lowercase()
        val queryName = name?.trim()?.lowercase()

        val result = _storages.value.flatMap { it.items }.filter { item ->
            val mgMatches = queryMG.isNullOrEmpty() || item.index.lowercase().contains(queryMG)
            val nameMatches = queryName.isNullOrEmpty() || item.name.lowercase().contains(queryName)
            mgMatches && nameMatches
        }

        _filteredItems.value = result
    }
}

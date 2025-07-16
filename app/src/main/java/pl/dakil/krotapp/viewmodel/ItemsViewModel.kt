package pl.dakil.krotapp.viewmodel

import android.content.Context
import android.util.Log
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

    private val _downloadStatus = MutableStateFlow(CsvFileHandler.Status.UNKNOWN)
    val downloadStatus: StateFlow<CsvFileHandler.Status> = _downloadStatus

    enum class SortOptions { NAME_AZ, NAME_ZA, AMOUNT_HIGH, AMOUNT_LOW }

    fun loadData(context: Context) {
        viewModelScope.launch {
            CsvFileHandler.maybeDownloadCsv(context) { status ->
                _downloadStatus.value = status

                if (status == CsvFileHandler.Status.SUCCESS)
                    _storages.value = CsvFileHandler.parseCsvFile(context)
            }

            _storages.value = CsvFileHandler.parseCsvFile(context)
            _filteredItems.value = emptyList()
        }
    }

    fun filterItems(mg: String?, name: String?) {
        val queryMG = mg?.trim()?.lowercase()
        val queryName = name?.trim()?.lowercase()

        val result = _storages.value.filter {
            queryMG.isNullOrEmpty() || it.mg.lowercase().contains(queryMG)
        }.flatMap { it.items }.filter {
            queryName.isNullOrEmpty() || it.name.lowercase().contains(queryName)
        }

        _filteredItems.value = result
        sortFilteredItems(SortOptions.NAME_AZ)
    }

    fun sortFilteredItems(option: SortOptions) {
        _filteredItems.value = _filteredItems.value.let { list ->
            when (option) {
                SortOptions.NAME_AZ -> list.sortedBy { it.name }
                SortOptions.NAME_ZA -> list.sortedByDescending { it.name }
                SortOptions.AMOUNT_LOW -> list.sortedBy { it.amount }
                SortOptions.AMOUNT_HIGH -> list.sortedByDescending { it.amount }
            }
        }
    }
}

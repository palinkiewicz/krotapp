package pl.dakil.krotapp.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pl.dakil.krotapp.R
import pl.dakil.krotapp.viewmodel.ItemsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    itemsViewModel: ItemsViewModel,
    onNavigateToList: () -> Unit
) {
    var selectedMG by rememberSaveable { mutableStateOf("") }
    var expanded by rememberSaveable { mutableStateOf(false) }
    var nameQuery by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = stringResource(R.string.search_title)) })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
            ) {
                OutlinedTextField(
                    value = selectedMG,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.mg_number)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    itemsViewModel.storages.value.forEach { storage ->
                        DropdownMenuItem(
                            text = { Text(storage.mg) },
                            onClick = {
                                selectedMG = storage.mg
                                expanded = false
                            }
                        )
                    }
                }
            }
            OutlinedTextField(
                value = nameQuery,
                onValueChange = { nameQuery = it },
                label = { Text(stringResource(R.string.item_name)) },
                modifier = Modifier
                    .fillMaxWidth()
            )
            Button(onClick = {
                itemsViewModel.filterItems(selectedMG, nameQuery)
                onNavigateToList()
            }) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "",
                )
                Text(text = stringResource(R.string.search_button))
            }
        }
    }
}

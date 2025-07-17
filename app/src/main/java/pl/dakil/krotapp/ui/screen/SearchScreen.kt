package pl.dakil.krotapp.ui.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.dakil.krotapp.R
import pl.dakil.krotapp.extension.getAppVersion
import pl.dakil.krotapp.viewmodel.ItemsViewModel
import pl.dakil.krotapp.viewmodel.UpdateCheckerViewModel

const val MG_KEY = "MG_VALUE"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    itemsViewModel: ItemsViewModel,
    onNavigateToList: () -> Unit
) {
    val context = LocalContext.current
    val updateViewModel: UpdateCheckerViewModel = viewModel()
    val updateStatus by updateViewModel.updateResult.collectAsState()

    val prefs = LocalContext.current.getSharedPreferences(stringResource(R.string.preferences_id), Context.MODE_PRIVATE)
    val storages by itemsViewModel.storages.collectAsState()

    var selectedMG by rememberSaveable { mutableStateOf(prefs.getString(MG_KEY, null) ?: "") }
    var expanded by rememberSaveable { mutableStateOf(false) }
    var nameQuery by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        updateViewModel.checkVersion(context.getAppVersion())
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = stringResource(R.string.search_title)) })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    storages.sortedBy { s -> s.mg.filter { it.isDigit() }.toInt() }.forEach { storage ->
                        DropdownMenuItem(
                            text = { Text(storage.mg) },
                            onClick = {
                                selectedMG = storage.mg
                                expanded = false
                                prefs.edit().putString(MG_KEY, storage.mg).apply()
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
                if (selectedMG != "") {
                    itemsViewModel.filterItems(selectedMG, nameQuery)
                    onNavigateToList()
                }
            }) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "",
                )
                Text(text = stringResource(R.string.search_button))
            }

            if (updateStatus.status == UpdateCheckerViewModel.Status.UPDATE) {
                Spacer(modifier = Modifier.height(16.dp))
                Card {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = stringResource(R.string.new_version_found), style = MaterialTheme.typography.bodyLarge)
                        OutlinedButton(onClick = {
                            updateStatus.link?.let { url ->
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            }
                        }) {
                            Text(text = stringResource(R.string.update_version))
                        }
                    }
                }
            }
        }
    }
}

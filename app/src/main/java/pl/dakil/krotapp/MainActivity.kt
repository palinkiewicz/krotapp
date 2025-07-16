package pl.dakil.krotapp

import AppNavGraph
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.dakil.krotapp.data.CsvFileHandler
import pl.dakil.krotapp.extension.showToast
import pl.dakil.krotapp.ui.theme.KrotAppTheme
import pl.dakil.krotapp.viewmodel.ItemsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            KrotAppTheme {
                val context = LocalContext.current
                val viewModel: ItemsViewModel = viewModel()

                val downloadStatus by viewModel.downloadStatus.collectAsState()

                LaunchedEffect(downloadStatus) {
                    when (downloadStatus) {
                        CsvFileHandler.Status.DOWNLOADING -> context.showToast(context.getString(R.string.downloading))
                        CsvFileHandler.Status.UP_TO_DATE -> context.showToast(context.getString(R.string.already_up_to_date))
                        CsvFileHandler.Status.SUCCESS -> context.showToast(context.getString(R.string.download_complete))
                        CsvFileHandler.Status.FAILED -> context.showToast(context.getString(R.string.download_failed))
                        else -> {}
                    }
                }

                LaunchedEffect(Unit) {
                    viewModel.loadData(context)
                }

                Surface {
                    AppNavGraph(viewModel)
                }
            }
        }
    }
}

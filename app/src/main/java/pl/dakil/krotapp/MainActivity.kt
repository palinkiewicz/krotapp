package pl.dakil.krotapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import pl.dakil.krotapp.data.XlsxFileHandler
import pl.dakil.krotapp.extension.showToast
import pl.dakil.krotapp.ui.theme.KrotAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            KrotAppTheme {
                val context = LocalContext.current

                LaunchedEffect(Unit) {
                    XlsxFileHandler.maybeDownloadXlsx(context) { status ->
                        when (status) {
                            XlsxFileHandler.Status.DOWNLOADING -> context.showToast("📥 Downloading...")
                            XlsxFileHandler.Status.UP_TO_DATE -> context.showToast("✅ Already up to date")
                            XlsxFileHandler.Status.SUCCESS -> context.showToast("✅ Download complete!")
                            XlsxFileHandler.Status.FAILED -> context.showToast("❌ Download failed!")
                        }
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Text(
                        text = "Hello!",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

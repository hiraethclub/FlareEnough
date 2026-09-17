package club.hiraeth.flareenough

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import club.hiraeth.flareenough.ui.FlareApp
import club.hiraeth.flareenough.ui.theme.FlareEnoughTheme

/**
 * The single Activity. It hosts all Compose UI. There are no other activities.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            FlareEnoughTheme {
                FlareApp()
            }
        }
    }
}

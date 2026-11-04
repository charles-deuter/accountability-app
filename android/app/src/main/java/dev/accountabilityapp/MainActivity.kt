package dev.accountabilityapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dev.accountabilityapp.theme.AccountabilityAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Force stop erases the app's alarms, and only the user opening the app can undo it: recompute them on every launch.
    val container = appContainerFrom(this)
    container.applicationScope.launch(block = { -> container.reminderSync.sync() })

    enableEdgeToEdge()
    setContent {
      AccountabilityAppTheme { Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { MainNavigation() } }
    }
  }
}

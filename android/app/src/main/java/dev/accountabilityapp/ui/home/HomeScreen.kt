package dev.accountabilityapp.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import dev.accountabilityapp.R
import dev.accountabilityapp.theme.AccountabilityAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
  Scaffold(modifier = modifier, topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) }) { innerPadding ->
    Box(Modifier.fillMaxSize().padding(innerPadding))
  }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
  AccountabilityAppTheme { HomeScreen() }
}

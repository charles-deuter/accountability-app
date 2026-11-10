package dev.accountabilityapp.ui.tasklist

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.accountabilityapp.R
import dev.accountabilityapp.appContainerFrom
import dev.accountabilityapp.theme.AccountabilityAppTheme
import dev.accountabilityapp.ui.formatDateTime
import java.time.Instant

@Composable
fun TaskListRoute(
  onAddTask: () -> Unit,
  onEditTask: (taskId: Long) -> Unit,
  onFixGrants: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val container = appContainerFrom(LocalContext.current)
  val viewModel: TaskListViewModel =
    viewModel(initializer = { ->
      TaskListViewModel(container.taskService, container.clock, container.permissionSource, Build.VERSION.SDK_INT)
    })
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  // The user can allow or revoke grants in Settings at any time, so re-check each time the app comes back.
  LifecycleResumeEffect(viewModel, effects = { ->
    viewModel.recheckGrants()
    this.onPauseOrDispose({ -> })
  })
  TaskListScreen(state = state, onAddTask = onAddTask, onEditTask = onEditTask, onFixGrants = onFixGrants, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
  state: TaskListUiState,
  onAddTask: () -> Unit,
  onEditTask: (taskId: Long) -> Unit,
  onFixGrants: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Scaffold(
    modifier = modifier,
    topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
    floatingActionButton = {
      FloatingActionButton(onClick = onAddTask) { Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_task)) }
    },
  ) { innerPadding ->
    Column(Modifier.fillMaxSize().padding(innerPadding)) {
      val grantWarning = state.grantWarning
      if (grantWarning != null) {
        GrantBanner(grantWarning, onClick = onFixGrants)
      }
      if (state.items.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
          Text(stringResource(R.string.task_list_empty), style = MaterialTheme.typography.bodyLarge)
        }
      } else {
        LazyColumn(Modifier.fillMaxSize()) {
          items(state.items, key = { item -> item.id }) { item ->
            TaskRow(item, onClick = { -> onEditTask(item.id) })
            HorizontalDivider()
          }
        }
      }
    }
  }
}

@Composable
private fun GrantBanner(warning: GrantWarning, onClick: () -> Unit) {
  val message =
    when (warning) {
      GrantWarning.REMINDERS_MAY_RUN_LATE -> R.string.banner_reminders_may_run_late
      GrantWarning.REMINDERS_MAY_NOT_RING -> R.string.banner_reminders_may_not_ring
    }
  Surface(color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
      Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
      Spacer(Modifier.width(12.dp))
      Text(stringResource(message), color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodyMedium)
    }
  }
}

@Composable
private fun TaskRow(item: TaskListItem, onClick: () -> Unit) {
  val overdueColor = MaterialTheme.colorScheme.error
  val accentColor = if (item.isOverdue) overdueColor else MaterialTheme.colorScheme.onSurfaceVariant
  val snoozedUntil = item.snoozedUntil
  val fireTimeLabel =
    if (snoozedUntil != null) {
      stringResource(R.string.snoozed_badge, item.snoozeCount, formatDateTime(snoozedUntil))
    } else {
      formatDateTime(item.fireTime)
    }
  val overdueLabel = stringResource(R.string.overdue)
  val supportingText = if (item.isOverdue) "$overdueLabel · $fireTimeLabel" else fireTimeLabel
  val headlineColor = if (item.isOverdue) overdueColor else MaterialTheme.colorScheme.onSurface
  ListItem(
    headlineContent = { Text(item.title, maxLines = 2, overflow = TextOverflow.Ellipsis) },
    supportingContent = { Text(supportingText, color = accentColor) },
    colors = ListItemDefaults.colors(headlineColor = headlineColor),
    modifier = Modifier.clickable(onClick = onClick),
  )
}

@Preview(showBackground = true)
@Composable
private fun TaskListScreenPreview() {
  AccountabilityAppTheme {
    TaskListScreen(
      state =
        TaskListUiState(
          listOf(
            TaskListItem(1, "Take meds", Instant.parse("2026-10-21T09:00:00Z"), isOverdue = true),
            TaskListItem(2, "Call dentist", Instant.parse("2026-10-22T15:00:00Z"), isOverdue = false),
            TaskListItem(
              3,
              "Pay rent",
              Instant.parse("2026-10-22T17:00:00Z"),
              isOverdue = false,
              snoozeCount = 3,
              snoozedUntil = Instant.parse("2026-10-22T17:00:00Z"),
            ),
          ),
          grantWarning = GrantWarning.REMINDERS_MAY_RUN_LATE,
        ),
      onAddTask = { -> },
      onEditTask = { _ -> },
      onFixGrants = { -> },
    )
  }
}

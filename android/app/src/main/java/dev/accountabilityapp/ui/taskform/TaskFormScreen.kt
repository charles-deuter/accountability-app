package dev.accountabilityapp.ui.taskform

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.accountabilityapp.R
import dev.accountabilityapp.appContainerFrom
import dev.accountabilityapp.data.TASK_TITLE_MAX_LENGTH
import dev.accountabilityapp.reminders.isNotificationPermissionMissing
import dev.accountabilityapp.ui.formatDate
import dev.accountabilityapp.ui.formatTime
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

@Composable
fun TaskFormRoute(taskId: Long?, onDone: () -> Unit, modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val container = appContainerFrom(context)
  val viewModel: TaskFormViewModel = viewModel(initializer = { -> TaskFormViewModel(container.taskService, container.clock, taskId) })
  val state by viewModel.uiState.collectAsStateWithLifecycle()

  LaunchedEffect(state.isFinished, block = { -> if (state.isFinished) onDone() })

  // Ask for notifications (Android 13+) as the user starts on a Reminder, so the first one can actually ring. If they
  // decline, the Task still saves; ticket 11 shows a warning while the permission is missing.
  val notificationPermissionRequest =
    rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission(), onResult = { _ -> })
  LaunchedEffect(Unit, block = { ->
    if (isNotificationPermissionMissing(context)) {
      notificationPermissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
  })

  TaskFormScreen(
    state = state,
    onTitleChange = viewModel::onTitleChange,
    onScheduledTimeChange = viewModel::onScheduledTimeChange,
    onSave = viewModel::save,
    onDelete = viewModel::delete,
    onBack = onDone,
    modifier = modifier,
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFormScreen(
  state: TaskFormUiState,
  onTitleChange: (String) -> Unit,
  onScheduledTimeChange: (Instant) -> Unit,
  onSave: () -> Unit,
  onDelete: () -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
  zone: ZoneId = ZoneId.systemDefault(),
) {
  var showDatePicker by remember({ -> mutableStateOf(false) })
  var showTimePicker by remember({ -> mutableStateOf(false) })
  var showDeleteConfirmation by remember({ -> mutableStateOf(false) })
  val screenTitle = if (state.isEditing) R.string.edit_task_title else R.string.add_task_title
  val scheduled = state.scheduledTime.atZone(zone)

  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(stringResource(screenTitle)) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.navigate_back))
          }
        },
        actions = {
          if (state.isEditing) {
            IconButton(onClick = { -> showDeleteConfirmation = true }) {
              Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete))
            }
          }
        },
      )
    }
  ) { innerPadding ->
    // While an existing Task loads, show nothing rather than a blank form that then jumps.
    if (state.isLoading) return@Scaffold
    Column(
      Modifier.padding(innerPadding).padding(16.dp).fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      OutlinedTextField(
        value = state.title,
        onValueChange = onTitleChange,
        label = { Text(stringResource(R.string.title_label)) },
        isError = state.showTitleRequired,
        supportingText = {
          Row {
            if (state.showTitleRequired) Text(stringResource(R.string.title_required))
            Spacer(Modifier.weight(1f))
            Text(stringResource(R.string.title_counter, state.title.length, TASK_TITLE_MAX_LENGTH))
          }
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
      )
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = { -> showDatePicker = true }) { Text(formatDate(state.scheduledTime, zone)) }
        OutlinedButton(onClick = { -> showTimePicker = true }) { Text(formatTime(state.scheduledTime, zone)) }
      }
      Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.save)) }
    }
  }

  if (showDatePicker) {
    // DatePicker works in UTC-midnight milliseconds for the selected calendar day.
    val initialDateMillis = scheduled.toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)
    DatePickerDialog(
      onDismissRequest = { -> showDatePicker = false },
      confirmButton = {
        TextButton(
          onClick = { ->
            datePickerState.selectedDateMillis?.let({ millis ->
              val pickedDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
              onScheduledTimeChange(combine(pickedDate, scheduled.toLocalTime(), zone))
            })
            showDatePicker = false
          }
        ) {
          Text(stringResource(R.string.ok))
        }
      },
      dismissButton = { TextButton(onClick = { -> showDatePicker = false }) { Text(stringResource(R.string.cancel)) } },
    ) {
      DatePicker(state = datePickerState)
    }
  }

  if (showDeleteConfirmation) {
    val confirmDelete: () -> Unit = { ->
      showDeleteConfirmation = false
      onDelete()
    }
    AlertDialog(
      onDismissRequest = { -> showDeleteConfirmation = false },
      title = { Text(stringResource(R.string.delete_confirmation)) },
      confirmButton = { TextButton(onClick = confirmDelete) { Text(stringResource(R.string.delete)) } },
      dismissButton = { TextButton(onClick = { -> showDeleteConfirmation = false }) { Text(stringResource(R.string.cancel)) } },
    )
  }

  if (showTimePicker) {
    val timePickerState = rememberTimePickerState(initialHour = scheduled.hour, initialMinute = scheduled.minute)
    AlertDialog(
      onDismissRequest = { -> showTimePicker = false },
      confirmButton = {
        TextButton(
          onClick = { ->
            val pickedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
            onScheduledTimeChange(combine(scheduled.toLocalDate(), pickedTime, zone))
            showTimePicker = false
          }
        ) {
          Text(stringResource(R.string.ok))
        }
      },
      dismissButton = { TextButton(onClick = { -> showTimePicker = false }) { Text(stringResource(R.string.cancel)) } },
      text = { TimePicker(state = timePickerState) },
    )
  }
}

private fun combine(date: LocalDate, time: LocalTime, zone: ZoneId): Instant = date.atTime(time).atZone(zone).toInstant()

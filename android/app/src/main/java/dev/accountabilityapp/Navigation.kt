package dev.accountabilityapp

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dev.accountabilityapp.ui.taskform.TaskFormRoute
import dev.accountabilityapp.ui.tasklist.TaskListRoute

@Composable
fun MainNavigation() {
  val backStack = rememberNavBackStack(TaskList)

  NavDisplay(
    backStack = backStack,
    onBack = { -> backStack.removeLastOrNull() },
    // Each back-stack entry gets its own ViewModels, cleared when the entry is popped. Without the ViewModel decorator they
    // belong to the Activity, so reopening a screen would reuse the previous visit's ViewModel.
    entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator(), rememberViewModelStoreNavEntryDecorator()),
    entryProvider =
      entryProvider {
        entry<TaskList> { TaskListRoute(onAddTask = { -> backStack.add(AddTask) }) }
        entry<AddTask> { TaskFormRoute(onDone = { -> backStack.removeLastOrNull() }) }
      },
  )
}

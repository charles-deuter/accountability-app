package dev.accountabilityapp

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import dev.accountabilityapp.ui.addtask.AddTaskRoute
import dev.accountabilityapp.ui.tasklist.TaskListRoute

@Composable
fun MainNavigation() {
  val backStack = rememberNavBackStack(TaskList)

  NavDisplay(
    backStack = backStack,
    onBack = { -> backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<TaskList> { TaskListRoute(onAddTask = { -> backStack.add(AddTask) }) }
        entry<AddTask> { AddTaskRoute(onDone = { -> backStack.removeLastOrNull() }) }
      },
  )
}

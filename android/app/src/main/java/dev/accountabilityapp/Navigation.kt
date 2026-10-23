package dev.accountabilityapp

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import dev.accountabilityapp.ui.addtask.AddTaskScreen
import dev.accountabilityapp.ui.tasklist.TaskListScreen

@Composable
fun MainNavigation() {
  val backStack = rememberNavBackStack(TaskList)

  NavDisplay(
    backStack = backStack,
    onBack = { -> backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<TaskList> { taskListKey -> TaskListScreen(onAddTask = { -> backStack.add(AddTask) }) }
        entry<AddTask> { addTaskKey -> AddTaskScreen(onDone = { -> backStack.removeLastOrNull() }) }
      },
  )
}

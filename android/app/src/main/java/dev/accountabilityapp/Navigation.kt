package dev.accountabilityapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dev.accountabilityapp.ui.onboarding.OnboardingRoute
import dev.accountabilityapp.ui.onboarding.isOnboardingComplete
import dev.accountabilityapp.ui.onboarding.markOnboardingComplete
import dev.accountabilityapp.ui.taskform.TaskFormRoute
import dev.accountabilityapp.ui.tasklist.TaskListRoute

@Composable
fun MainNavigation() {
  val context = LocalContext.current
  // Read once: only the start key depends on it, and it is a disk read.
  val isFirstLaunch = remember({ -> !isOnboardingComplete(context) })
  val startKey = if (isFirstLaunch) Onboarding else TaskList
  val backStack = rememberNavBackStack(startKey)
  // On first launch Onboarding is the only entry, so finishing puts the Task List beneath it before leaving. Opened from
  // the Task List banner, it simply goes back.
  val finishOnboarding: () -> Unit = { ->
    markOnboardingComplete(context)
    if (TaskList !in backStack) backStack.add(0, TaskList)
    backStack.removeLastOrNull()
  }

  NavDisplay(
    backStack = backStack,
    onBack = { -> backStack.removeLastOrNull() },
    // Each back-stack entry gets its own ViewModels, cleared when the entry is popped. Without the ViewModel decorator they
    // belong to the Activity, so reopening a screen would reuse the previous visit's ViewModel.
    entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator(), rememberViewModelStoreNavEntryDecorator()),
    entryProvider =
      entryProvider {
        entry<Onboarding> { OnboardingRoute(onFinish = finishOnboarding) }
        entry<TaskList> {
          TaskListRoute(
            onAddTask = { -> backStack.add(AddTask) },
            onEditTask = { taskId -> backStack.add(EditTask(taskId)) },
            onFixGrants = { -> if (Onboarding !in backStack) backStack.add(Onboarding) },
          )
        }
        entry<AddTask> { TaskFormRoute(taskId = null, onDone = { -> backStack.removeLastOrNull() }) }
        entry<EditTask> { editTaskKey -> TaskFormRoute(taskId = editTaskKey.taskId, onDone = { -> backStack.removeLastOrNull() }) }
      },
  )
}

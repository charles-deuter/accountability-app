package dev.accountabilityapp

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Onboarding : NavKey

@Serializable data object TaskList : NavKey

@Serializable data object AddTask : NavKey

@Serializable data class EditTask(val taskId: Long) : NavKey

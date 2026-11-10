package dev.accountabilityapp.ui.onboarding

import android.content.Context
import androidx.core.content.edit

private const val PREFERENCES_NAME = "onboarding"
private const val KEY_COMPLETE = "complete"

/**
 * Onboarding opens the app until the user finishes it once. After that, the Task List banner is how missing grants
 * surface.
 */
fun isOnboardingComplete(context: Context): Boolean {
  val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
  return preferences.getBoolean(KEY_COMPLETE, false)
}

fun markOnboardingComplete(context: Context) {
  val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
  preferences.edit(action = { -> this.putBoolean(KEY_COMPLETE, true) })
}

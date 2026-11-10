package dev.accountabilityapp.ui.onboarding

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

private const val PREFERENCES_NAME = "onboarding"
private const val KEY_COMPLETE = "complete"

/**
 * Onboarding opens the app until the user finishes it once. After that, the Task List banner is how missing grants
 * surface.
 */
fun isOnboardingComplete(context: Context): Boolean = onboardingPreferencesFrom(context).getBoolean(KEY_COMPLETE, false)

fun markOnboardingComplete(context: Context) {
  onboardingPreferencesFrom(context).edit(action = { -> this.putBoolean(KEY_COMPLETE, true) })
}

private fun onboardingPreferencesFrom(context: Context): SharedPreferences =
  context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

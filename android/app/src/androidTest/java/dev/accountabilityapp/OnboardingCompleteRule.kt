package dev.accountabilityapp

import androidx.test.core.app.ApplicationProvider
import dev.accountabilityapp.ui.onboarding.markOnboardingComplete
import org.junit.rules.ExternalResource

/**
 * Skips first-launch onboarding, for tests that open the app on the Task List. Give it `order = 0` so it runs before a
 * compose rule launches the activity.
 */
class OnboardingCompleteRule : ExternalResource() {
  override fun before() {
    markOnboardingComplete(ApplicationProvider.getApplicationContext())
  }
}

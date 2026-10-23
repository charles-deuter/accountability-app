// Top-level build file where you can add configuration options common to all sub-projects/modules.
// Lock the plugin classpath (AGP, Kotlin, KSP and their transitive dependencies) in buildscript-gradle.lockfile.
buildscript {
  configurations.classpath { resolutionStrategy.activateDependencyLocking() }
}

plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.compose.compiler) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.ksp) apply false
}

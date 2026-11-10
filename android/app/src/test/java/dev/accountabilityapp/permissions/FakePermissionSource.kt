package dev.accountabilityapp.permissions

/** A [PermissionSource] whose answers the test sets directly; nothing is granted until the test says so. */
class FakePermissionSource(vararg grantedAtStart: Grant) : PermissionSource {
  val granted: MutableSet<Grant> = grantedAtStart.toMutableSet()

  override fun isGranted(grant: Grant): Boolean = grant in granted
}

package dev.accountabilityapp.ui

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
private val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
private val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

fun Instant.formatDateTime(zone: ZoneId = ZoneId.systemDefault()): String = dateTimeFormatter.format(this.atZone(zone))

fun Instant.formatDate(zone: ZoneId = ZoneId.systemDefault()): String = dateFormatter.format(this.atZone(zone))

fun Instant.formatTime(zone: ZoneId = ZoneId.systemDefault()): String = timeFormatter.format(this.atZone(zone))

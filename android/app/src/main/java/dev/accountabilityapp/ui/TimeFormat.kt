package dev.accountabilityapp.ui

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
private val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
private val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

fun formatDateTime(instant: Instant, zone: ZoneId = ZoneId.systemDefault()): String = dateTimeFormatter.format(instant.atZone(zone))

fun formatDate(instant: Instant, zone: ZoneId = ZoneId.systemDefault()): String = dateFormatter.format(instant.atZone(zone))

fun formatTime(instant: Instant, zone: ZoneId = ZoneId.systemDefault()): String = timeFormatter.format(instant.atZone(zone))

fun formatTimeOfDay(timeOfDay: LocalTime): String = timeFormatter.format(timeOfDay)

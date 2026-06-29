package com.example.calibre_zenith.data

import java.util.Calendar

data class DynamicPlannerTask(
    val id: String,
    val title: String,
    val microStep: String,
    val frictionNotes: String,
    val dayOfYear: Int,
    val year: Int,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val isHighFriction: Boolean
)

fun TaskNode.toDynamicTask(): DynamicPlannerTask? {
    if (this.scheduledDate.isNullOrBlank() || this.scheduledTime.isNullOrBlank()) return null
    val parts = this.scheduledDate!!.split("-")
    if (parts.size != 3) return null
    val cal = Calendar.getInstance()
    cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())

    val timeParts = this.scheduledTime!!.split(":")
    val endParts = this.scheduledEndTime?.split(":")
    
    return DynamicPlannerTask(
        id = this.id,
        title = this.title,
        microStep = this.details,
        frictionNotes = this.flairs.joinToString(", "),
        dayOfYear = cal.get(Calendar.DAY_OF_YEAR),
        year = cal.get(Calendar.YEAR),
        startHour = timeParts[0].toInt(),
        startMinute = timeParts[1].toInt(),
        endHour = endParts?.get(0)?.toInt() ?: (timeParts[0].toInt() + 1),
        endMinute = endParts?.get(1)?.toInt() ?: timeParts[1].toInt(),
        isHighFriction = false
    )
}
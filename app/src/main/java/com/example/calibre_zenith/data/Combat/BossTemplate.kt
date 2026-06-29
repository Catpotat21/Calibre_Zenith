package com.example.calibre_zenith.data.combat

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "boss_templates")
data class BossTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val baseHp: Int,
    val bossImageUrl: String = "",  // URL pasted by user
    val tags: String = ""           // comma-separated tag names e.g. "Study,Health"
) {
    // Helper to get tags as a list
    fun tagList(): List<String> =
        if (tags.isBlank()) emptyList()
        else tags.split(",").map { it.trim() }.filter { it.isNotBlank() }
}
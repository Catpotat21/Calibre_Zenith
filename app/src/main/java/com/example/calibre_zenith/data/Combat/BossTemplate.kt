package com.example.calibre_zenith.data.combat

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "boss_templates")
data class BossTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val baseHp: Int,
    val bossImageUrl: String
)
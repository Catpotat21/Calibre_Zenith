package com.example.calibre_zenith.data.combat

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "active_boss",
    foreignKeys = [
        ForeignKey(
            entity = BossTemplate::class,
            parentColumns = ["id"],
            childColumns = ["bossTemplateId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ActiveBoss(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val currentHp: Int,
    val bossTemplateId: Int
)
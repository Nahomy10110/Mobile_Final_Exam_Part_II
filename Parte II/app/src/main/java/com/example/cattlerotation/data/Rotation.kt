package com.example.cattlerotation.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rotations",
    foreignKeys = [ForeignKey(
        entity = Paddock::class,
        parentColumns = ["id"],
        childColumns = ["paddockId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("paddockId")]
)
data class Rotation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val paddockId: Long,
    val startDate: Long
)
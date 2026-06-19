package com.example.cattlerotation.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "paddocks")
data class Paddock(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val areaM2: Double,
    val creationDate: Long,
    val photoPath: String? = null,
    val videoPath: String? = null
)
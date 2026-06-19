package com.example.cattlerotation.data

import androidx.room.*

@Dao
interface RotationDao {
    @Query("SELECT * FROM rotations ORDER BY startDate")
    suspend fun getAll(): List<Rotation>

    @Insert suspend fun insert(rotation: Rotation): Long
}
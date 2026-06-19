package com.example.cattlerotation.data

import androidx.room.*

@Dao
interface PaddockDao {
    @Query("SELECT * FROM paddocks ORDER BY name")
    suspend fun getAll(): List<Paddock>

    @Query("SELECT * FROM paddocks WHERE id = :id")
    suspend fun getById(id: Long): Paddock?

    @Query("SELECT COUNT(*) FROM paddocks")
    suspend fun count(): Int

    @Insert suspend fun insert(paddock: Paddock): Long
    @Update suspend fun update(paddock: Paddock)
    @Delete suspend fun delete(paddock: Paddock)
}
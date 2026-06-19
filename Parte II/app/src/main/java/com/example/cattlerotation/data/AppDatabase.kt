package com.example.cattlerotation.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Paddock::class, Rotation::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun paddockDao(): PaddockDao
    abstract fun rotationDao(): RotationDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cattle_rotation.db"
                ).build().also { INSTANCE = it }
            }
    }
}
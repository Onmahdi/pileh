package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        InspirationEntity::class,
        IdeaEntity::class,
        PostEntity::class,
        TaskEntity::class,
        WikiEntity::class,
        BrandAssetEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun inspirationDao(): InspirationDao
    abstract fun ideaDao(): IdeaDao
    abstract fun postDao(): PostDao
    abstract fun taskDao(): TaskDao
    abstract fun wikiDao(): WikiDao
    abstract fun brandAssetDao(): BrandAssetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pileh_os_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

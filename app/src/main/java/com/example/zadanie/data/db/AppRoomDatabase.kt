package com.example.zadanie.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.zadanie.data.db.model.BarItem
import com.example.zadanie.data.db.model.Follower
import com.example.zadanie.data.db.model.Friend

@Database(entities = [BarItem::class, Friend::class, Follower::class], version = 7, exportSchema = false)
abstract class AppRoomDatabase: RoomDatabase() {
    abstract fun appDao(): DbDao

    companion object{
        @Volatile
        private var INSTANCE: AppRoomDatabase? = null

        fun getInstance(context: Context): AppRoomDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also {  INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                AppRoomDatabase::class.java, "barsDatabase"
            ).fallbackToDestructiveMigration()
                .build()
    }
}
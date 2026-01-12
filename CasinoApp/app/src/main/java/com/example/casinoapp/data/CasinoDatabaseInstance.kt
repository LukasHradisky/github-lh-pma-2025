package com.example.casinoapp.data

import android.content.Context
import androidx.room.Room

object CasinoDatabaseInstance {
    @Volatile
    private var INSTANCE: CasinoDatabase? = null

    fun getDatabase(context: Context): CasinoDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                CasinoDatabase::class.java,
                "casino_database"
            )
                .fallbackToDestructiveMigration()
                .build()
            INSTANCE = instance
            instance
        }
    }
}
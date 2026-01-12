package com.example.casinoapp.data



import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [User::class], version = 2, exportSchema = false)
abstract class CasinoDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}
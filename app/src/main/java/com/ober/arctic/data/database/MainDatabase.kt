package com.ober.arctic.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ober.arctic.data.model.EncryptedDataHolder
import com.ober.arctic.data.model.EncryptedDataHolderDao

@Database(
    entities = [
        EncryptedDataHolder::class
    ], version = 1
)
abstract class MainDatabase : RoomDatabase() {
    abstract fun encryptedDataHolderDao(): EncryptedDataHolderDao
}
package com.ober.arctic.data.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import com.ober.arctic.data.database.BaseDao

@Entity
class EncryptedDataHolder(
    val salt: String,
    val encryptedJson: String
) {
    @PrimaryKey
    var id: String = "singleId"
}

@Dao
interface EncryptedDataHolderDao : BaseDao<EncryptedDataHolder> {
    @Query("SELECT * from EncryptedDataHolder LIMIT 1")
    fun getEncryptedDataHolder(): LiveData<EncryptedDataHolder>
}
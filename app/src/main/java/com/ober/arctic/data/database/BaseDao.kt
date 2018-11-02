package com.ober.arctic.data.database

import androidx.room.*

@Dao
interface BaseDao<T> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(t: T)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg t: T)

    @Update
    fun update(t: T)

    @Delete
    fun delete(t: T)
}
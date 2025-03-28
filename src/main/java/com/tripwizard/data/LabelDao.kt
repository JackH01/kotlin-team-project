package com.tripwizard.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LabelDao {
    @Insert
    suspend fun insert(label: Label)

    @Update
    suspend fun update(label: Label)

    @Delete
    suspend fun delete(label: Label)

    @Query("SELECT * from labels WHERE id = :id")
    fun getLabel(id: Int): Flow<Label>

    @Query("SELECT * from labels")
    fun getAllLabels(): Flow<List<Label>>
}
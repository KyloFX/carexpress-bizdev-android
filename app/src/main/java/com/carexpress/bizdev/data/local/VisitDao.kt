package com.carexpress.bizdev.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitDao {
    @Query("SELECT * FROM visits ORDER BY visitTimestamp DESC")
    fun observeAll(): Flow<List<VisitEntity>>

    @Query("SELECT * FROM visits WHERE id = :id")
    suspend fun getById(id: Long): VisitEntity?

    @Query("SELECT DISTINCT organisation FROM visits ORDER BY organisation")
    fun observeOrganisations(): Flow<List<String>>

    @Upsert
    suspend fun upsert(visit: VisitEntity): Long

    @Insert
    suspend fun insertAll(visits: List<VisitEntity>)

    @Delete
    suspend fun delete(visit: VisitEntity)

    @Query("SELECT COUNT(*) FROM visits")
    suspend fun count(): Int
}

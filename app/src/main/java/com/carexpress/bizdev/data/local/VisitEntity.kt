package com.carexpress.bizdev.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "visits", indices = [Index("visitTimestamp"), Index("segment"), Index("stage")])
data class VisitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val organisation: String,
    val segment: Segment,
    val keyPerson: String,
    val role: String,
    val visitType: VisitType,
    val stage: Stage,
    val coveredLives: Int,
    val monthlyValue: Double,
    val leadScore: Int,
    val notes: String,
    val nextAction: String,
    val followUpDate: LocalDate?,
    /** When the meeting happened (auto-stamped on modal open, overridable for back-logging). */
    val visitTimestamp: LocalDateTime,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)

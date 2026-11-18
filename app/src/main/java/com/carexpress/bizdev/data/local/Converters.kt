package com.carexpress.bizdev.data.local

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class Converters {
    private val zone: ZoneId get() = ZoneId.systemDefault()

    @TypeConverter fun fromDateTime(v: LocalDateTime?): Long? = v?.atZone(zone)?.toInstant()?.toEpochMilli()
    @TypeConverter fun toDateTime(v: Long?): LocalDateTime? = v?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(it), zone) }

    @TypeConverter fun fromDate(v: LocalDate?): Long? = v?.toEpochDay()
    @TypeConverter fun toDate(v: Long?): LocalDate? = v?.let(LocalDate::ofEpochDay)

    @TypeConverter fun fromSegment(v: Segment): String = v.name
    @TypeConverter fun toSegment(v: String): Segment = Segment.valueOf(v)

    @TypeConverter fun fromStage(v: Stage): String = v.name
    @TypeConverter fun toStage(v: String): Stage = Stage.valueOf(v)

    @TypeConverter fun fromVisitType(v: VisitType): String = v.name
    @TypeConverter fun toVisitType(v: String): VisitType = VisitType.valueOf(v)
}

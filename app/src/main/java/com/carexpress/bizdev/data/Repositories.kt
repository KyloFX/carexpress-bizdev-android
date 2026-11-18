package com.carexpress.bizdev.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.carexpress.bizdev.data.local.VisitDao
import com.carexpress.bizdev.data.local.VisitEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VisitRepository @Inject constructor(private val dao: VisitDao) {
    val visits: Flow<List<VisitEntity>> = dao.observeAll()
    val knownOrganisations: Flow<List<String>> = dao.observeOrganisations()

    suspend fun get(id: Long) = dao.getById(id)

    suspend fun save(visit: VisitEntity): Long =
        dao.upsert(visit.copy(updatedAt = LocalDateTime.now()))

    suspend fun delete(visit: VisitEntity) = dao.delete(visit)
}

private val Context.dataStore by preferencesDataStore("bizdev_settings")

@Singleton
class SettingsRepository @Inject constructor(@ApplicationContext private val context: Context) {
    private val listingsKey = intPreferencesKey("medical_aid_listings")

    /** Manual milestone counter for Medical Aid Direct Billing Network Listings (default 5 of 6). */
    val medicalAidListings: Flow<Int> = context.dataStore.data.map { it[listingsKey] ?: DEFAULT_LISTINGS }

    suspend fun setMedicalAidListings(value: Int) {
        context.dataStore.edit { it[listingsKey] = value.coerceIn(0, 99) }
    }

    companion object { const val DEFAULT_LISTINGS = 5 }
}

package com.carexpress.bizdev.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.carexpress.bizdev.data.local.AppDatabase
import com.carexpress.bizdev.data.local.VisitDao
import com.carexpress.bizdev.data.seed.SeedData
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        daoProvider: Provider<VisitDao>,
    ): AppDatabase = Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
        .addCallback(SeedCallback(daoProvider))
        .build()

    @Provides
    fun provideVisitDao(db: AppDatabase): VisitDao = db.visitDao()
}

/** Pre-populates the target accounts the first time the database file is created. */
private class SeedCallback(private val daoProvider: Provider<VisitDao>) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            val dao = daoProvider.get()
            if (dao.count() == 0) dao.insertAll(SeedData.visits())
        }
    }
}

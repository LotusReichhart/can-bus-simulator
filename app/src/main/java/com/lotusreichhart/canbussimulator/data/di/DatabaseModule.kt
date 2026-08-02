package com.lotusreichhart.canbussimulator.data.di

import android.content.Context
import com.lotusreichhart.canbussimulator.data.database.CanFrameDao
import com.lotusreichhart.canbussimulator.data.database.CanFrameDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): CanFrameDatabase {
        return CanFrameDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideCanFrameDao(
        database: CanFrameDatabase
    ): CanFrameDao {
        return database.canFrameDao()
    }
}

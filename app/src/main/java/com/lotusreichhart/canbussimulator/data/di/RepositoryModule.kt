package com.lotusreichhart.canbussimulator.data.di

import com.lotusreichhart.canbussimulator.data.repository.CanFrameRepositoryImpl
import com.lotusreichhart.canbussimulator.domain.repository.CanFrameRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCanFrameRepository(
        canFrameRepositoryImpl: CanFrameRepositoryImpl
    ): CanFrameRepository
}

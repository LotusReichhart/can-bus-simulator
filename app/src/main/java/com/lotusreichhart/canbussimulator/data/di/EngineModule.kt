package com.lotusreichhart.canbussimulator.data.di

import com.lotusreichhart.canbussimulator.data.jni.NativeCalculator
import com.lotusreichhart.canbussimulator.domain.service.ChecksumCalculator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class EngineModule {

    @Binds
    @Singleton
    abstract fun bindChecksumCalculator(
        nativeCalculator: NativeCalculator
    ): ChecksumCalculator
}

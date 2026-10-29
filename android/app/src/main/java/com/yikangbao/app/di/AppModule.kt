package com.yikangbao.app.di

import com.yikangbao.app.data.api.YiKangBaoApi
import com.yikangbao.app.data.repository.AuthRepositoryImpl
import com.yikangbao.app.data.repository.ChatRepositoryImpl
import com.yikangbao.app.data.repository.HealthRecordRepositoryImpl
import com.yikangbao.app.data.repository.ReminderRepositoryImpl
import com.yikangbao.app.domain.repository.AuthRepository
import com.yikangbao.app.domain.repository.ChatRepository
import com.yikangbao.app.domain.repository.HealthRecordRepository
import com.yikangbao.app.domain.repository.ReminderRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Configured via android/gradle.properties (BASE_URL) and overridable in
    // android/local.properties. Falls back to a local dev server.
    private const val BASE_URL =
        if (BuildConfig.BASE_URL.isNotBlank()) BuildConfig.BASE_URL else "http://10.0.2.2:8000/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideYiKangBaoApi(retrofit: Retrofit): YiKangBaoApi {
        return retrofit.create(YiKangBaoApi::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository

    @Binds
    @Singleton
    abstract fun bindHealthRecordRepository(impl: HealthRecordRepositoryImpl): HealthRecordRepository

    @Binds
    @Singleton
    abstract fun bindReminderRepository(impl: ReminderRepositoryImpl): ReminderRepository
}

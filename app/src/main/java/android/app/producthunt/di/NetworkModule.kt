package android.app.producthunt.di

import android.app.producthunt.BuildConfig
import android.app.producthunt.data.remote.api.AuthApiService
import android.app.producthunt.data.remote.api.PlatformProductApiService
import android.app.producthunt.data.remote.api.PriceAlertApiService
import android.app.producthunt.data.remote.api.PriceRecordApiService
import android.app.producthunt.data.remote.api.ProductApiService
import android.app.producthunt.data.remote.api.WishlistApiService
import android.app.producthunt.data.remote.interceptor.AuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    @Provides @Singleton
    fun provideProductApiService(retrofit: Retrofit): ProductApiService =
        retrofit.create(ProductApiService::class.java)

    @Provides @Singleton
    fun providePlatformProductApiService(retrofit: Retrofit): PlatformProductApiService =
        retrofit.create(PlatformProductApiService::class.java)

    @Provides @Singleton
    fun providePriceRecordApiService(retrofit: Retrofit): PriceRecordApiService =
        retrofit.create(PriceRecordApiService::class.java)

    @Provides @Singleton
    fun providePriceAlertApiService(retrofit: Retrofit): PriceAlertApiService =
        retrofit.create(PriceAlertApiService::class.java)

    @Provides @Singleton
    fun provideWishlistApiService(retrofit: Retrofit): WishlistApiService =
        retrofit.create(WishlistApiService::class.java)
}

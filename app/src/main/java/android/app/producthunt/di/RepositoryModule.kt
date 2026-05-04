package android.app.producthunt.di

import android.app.producthunt.data.local.TokenDataStore
import android.app.producthunt.data.remote.api.AuthApiService
import android.app.producthunt.data.remote.api.PlatformProductApiService
import android.app.producthunt.data.remote.api.PriceAlertApiService
import android.app.producthunt.data.remote.api.PriceRecordApiService
import android.app.producthunt.data.remote.api.ProductApiService
import android.app.producthunt.data.remote.api.WishlistApiService
import android.app.producthunt.data.repository.AuthRepository
import android.app.producthunt.data.repository.PlatformProductRepository
import android.app.producthunt.data.repository.PriceAlertRepository
import android.app.producthunt.data.repository.PriceRecordRepository
import android.app.producthunt.data.repository.ProductRepository
import android.app.producthunt.data.repository.WishlistRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides @Singleton
    fun provideAuthRepository(api: AuthApiService, tokenDataStore: TokenDataStore): AuthRepository =
        AuthRepository(api, tokenDataStore)

    @Provides @Singleton
    fun provideProductRepository(api: ProductApiService): ProductRepository =
        ProductRepository(api)

    @Provides @Singleton
    fun providePlatformProductRepository(api: PlatformProductApiService): PlatformProductRepository =
        PlatformProductRepository(api)

    @Provides @Singleton
    fun providePriceRecordRepository(api: PriceRecordApiService): PriceRecordRepository =
        PriceRecordRepository(api)

    @Provides @Singleton
    fun providePriceAlertRepository(api: PriceAlertApiService): PriceAlertRepository =
        PriceAlertRepository(api)

    @Provides @Singleton
    fun provideWishlistRepository(api: WishlistApiService): WishlistRepository =
        WishlistRepository(api)
}

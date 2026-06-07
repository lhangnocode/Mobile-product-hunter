package android.app.producthunt.widget

import android.app.producthunt.data.repository.PlatformProductRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface DealsWidgetEntryPoint {
    fun platformProductRepository(): PlatformProductRepository
}

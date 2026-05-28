package android.app.producthunt.di

import android.app.producthunt.data.local.db.ProductHuntDatabase
import android.app.producthunt.data.local.db.dao.AgentConversationDao
import android.content.Context
import androidx.room.Room
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
    fun provideProductHuntDatabase(
        @ApplicationContext context: Context,
    ): ProductHuntDatabase =
        Room.databaseBuilder(
            context,
            ProductHuntDatabase::class.java,
            "product_hunt.db",
        ).build()

    @Provides
    fun provideAgentConversationDao(
        database: ProductHuntDatabase,
    ): AgentConversationDao =
        database.agentConversationDao()
}

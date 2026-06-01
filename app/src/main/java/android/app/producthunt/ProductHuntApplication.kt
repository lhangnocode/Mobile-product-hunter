package android.app.producthunt

import android.app.producthunt.core.agent.AgentOrchestrator
import android.app.producthunt.core.agent.AgentToolSet
import android.app.producthunt.core.log.ILog
import android.app.producthunt.data.repository.AgentConversationRepository
import android.app.producthunt.data.repository.PlatformProductRepository
import android.app.producthunt.data.repository.PriceRecordRepository
import android.app.producthunt.data.repository.ProductRepository
import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.google.ai.edge.litertlm.tool
import javax.inject.Inject

@HiltAndroidApp
class ProductHuntApplication : Application() {
    @Inject lateinit var agentConversationRepository: AgentConversationRepository
    @Inject lateinit var productRepository: ProductRepository
    @Inject lateinit var platformProductRepository: PlatformProductRepository
    @Inject lateinit var priceRecordRepository: PriceRecordRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        val agentToolSet = AgentToolSet(
            productRepository = productRepository,
            platformProductRepository = platformProductRepository,
            priceRecordRepository = priceRecordRepository,
        )

        AgentOrchestrator.configure(
            context = this,
            conversationRepository = agentConversationRepository,
            toolProviders = listOf(
                tool(agentToolSet)
            ),
        )

        applicationScope.launch {
            val result = AgentOrchestrator.init()
            result.onFailure { ILog.e(TAG, "onCreate", "startup agent init failed", throwable = it) }
        }
    }

    override fun onTerminate() {
        AgentOrchestrator.close()
        super.onTerminate()
    }

    private companion object {
        private const val TAG = "Application"
    }
}

package android.app.producthunt

import android.app.producthunt.core.agent.AgentOrchestrator
import android.app.producthunt.core.log.ILog
import android.app.producthunt.data.repository.AgentConversationRepository
import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class ProductHuntApplication : Application() {
    @Inject lateinit var agentConversationRepository: AgentConversationRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        AgentOrchestrator.configure(
            context = this,
            conversationRepository = agentConversationRepository,
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

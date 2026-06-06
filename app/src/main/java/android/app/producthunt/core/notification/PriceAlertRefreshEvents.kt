package android.app.producthunt.core.notification

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PriceAlertRefreshEvents @Inject constructor() {
    private val _events = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
    )
    val events: SharedFlow<Unit> = _events.asSharedFlow()

    fun requestRefresh() {
        _events.tryEmit(Unit)
    }
}

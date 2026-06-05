package android.app.producthunt.core.notification

import android.app.producthunt.data.local.ThemePreferencesDataStore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class ProductHunterFirebaseMessagingService : FirebaseMessagingService() {
    @Inject lateinit var fcmTokenRegistrar: FcmTokenRegistrar
    @Inject lateinit var notificationNotifier: PriceAlertNotificationNotifier
    @Inject lateinit var themePreferencesDataStore: ThemePreferencesDataStore
    @Inject lateinit var priceAlertRefreshEvents: PriceAlertRefreshEvents

    override fun onNewToken(token: String) {
        runBlocking {
            fcmTokenRegistrar.registerTokenIfAuthenticated(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val payload = PriceAlertNotificationPayload.fromData(message.data) ?: return
        priceAlertRefreshEvents.requestRefresh()

        val notificationsEnabled = runBlocking {
            themePreferencesDataStore.priceAlertNotificationsEnabled.first()
        }
        if (!notificationsEnabled) return

        val title = message.data["title"]
            ?: message.notification?.title
            ?: "Price alert reached"
        val body = message.data["body"]
            ?: message.notification?.body
            ?: "A tracked product is now at or below your target price."

        notificationNotifier.showRemotePriceAlert(
            title = title,
            body = body,
            payload = payload,
        )
    }
}

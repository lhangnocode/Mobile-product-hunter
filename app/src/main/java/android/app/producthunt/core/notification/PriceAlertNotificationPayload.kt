package android.app.producthunt.core.notification

import android.content.Intent

data class PriceAlertNotificationPayload(
    val productId: String,
    val platformProductId: String?,
    val imageUrl: String? = null,
    val productName: String? = null,
) {
    companion object {
        const val TYPE_PRICE_ALERT = "price_alert"
        const val EXTRA_TYPE = "notification_type"
        const val EXTRA_PRODUCT_ID = "product_id"
        const val EXTRA_PLATFORM_PRODUCT_ID = "platform_product_id"
        const val EXTRA_IMAGE_URL = "image_url"
        const val EXTRA_PRODUCT_NAME = "product_name"

        fun fromData(data: Map<String, String>): PriceAlertNotificationPayload? {
            val type = data[EXTRA_TYPE] ?: data["type"]
            if (type != TYPE_PRICE_ALERT) return null

            val productId = data[EXTRA_PRODUCT_ID]?.takeIf { it.isNotBlank() } ?: return null
            return PriceAlertNotificationPayload(
                productId = productId,
                platformProductId = data[EXTRA_PLATFORM_PRODUCT_ID]?.takeIf { it.isNotBlank() },
                imageUrl = data[EXTRA_IMAGE_URL]?.takeIf { it.isNotBlank() }
                    ?: data["main_image_url"]?.takeIf { it.isNotBlank() },
                productName = data[EXTRA_PRODUCT_NAME]?.takeIf { it.isNotBlank() },
            )
        }

        fun fromIntent(intent: Intent?): PriceAlertNotificationPayload? {
            if (intent == null) return null

            val type = intent.getStringExtra(EXTRA_TYPE) ?: intent.getStringExtra("type")
            if (type != TYPE_PRICE_ALERT) return null

            val productId = intent.getStringExtra(EXTRA_PRODUCT_ID)
                ?.takeIf { it.isNotBlank() }
                ?: return null

            return PriceAlertNotificationPayload(
                productId = productId,
                platformProductId = intent.getStringExtra(EXTRA_PLATFORM_PRODUCT_ID)
                    ?.takeIf { it.isNotBlank() },
                imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL)
                    ?.takeIf { it.isNotBlank() },
                productName = intent.getStringExtra(EXTRA_PRODUCT_NAME)
                    ?.takeIf { it.isNotBlank() },
            )
        }
    }
}

fun Intent.putPriceAlertPayload(payload: PriceAlertNotificationPayload): Intent =
    putExtra(PriceAlertNotificationPayload.EXTRA_TYPE, PriceAlertNotificationPayload.TYPE_PRICE_ALERT)
        .putExtra(PriceAlertNotificationPayload.EXTRA_PRODUCT_ID, payload.productId)
        .putExtra(PriceAlertNotificationPayload.EXTRA_PLATFORM_PRODUCT_ID, payload.platformProductId)
        .putExtra(PriceAlertNotificationPayload.EXTRA_IMAGE_URL, payload.imageUrl)
        .putExtra(PriceAlertNotificationPayload.EXTRA_PRODUCT_NAME, payload.productName)

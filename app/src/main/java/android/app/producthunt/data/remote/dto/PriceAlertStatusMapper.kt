package android.app.producthunt.data.remote.dto

object PriceAlertStatusMapper {
    fun displayCurrentPrice(alert: PriceAlertResponse): Double? =
        alert.currentPrice
            ?: alert.lastCheckedPrice
            ?: alert.latestPrice
            ?: alert.price

    fun isTargetReached(alert: PriceAlertResponse): Boolean {
        val normalizedStatus = alert.status?.trim()?.lowercase().orEmpty()
        return (
            alert.targetReached == true ||
                alert.isTargetReached == true ||
                alert.triggered == true ||
                alert.isTriggered == true ||
                !alert.triggeredAt.isNullOrBlank() ||
                !alert.lastTriggeredAt.isNullOrBlank() ||
                !alert.notifiedAt.isNullOrBlank() ||
                !alert.lastNotifiedAt.isNullOrBlank() ||
                normalizedStatus in reachedStatuses
            )
    }

    fun statusText(alert: PriceAlertResponse): String =
        if (isTargetReached(alert)) "Target reached!" else "Waiting for price drop"

    private val reachedStatuses = setOf(
        "1",
        "triggered",
        "is_triggered",
        "target_reached",
        "target reached",
        "reached",
    )
}

package android.app.producthunt.data.remote.dto

object PriceAlertStatusMapper {
    fun displayCurrentPrice(alert: PriceAlertResponse): Double? =
        alert.currentPrice
            ?: alert.lastCheckedPrice
            ?: alert.latestPrice
            ?: alert.price

    fun isTargetReached(alert: PriceAlertResponse): Boolean {
        val currentPrice = displayCurrentPrice(alert)
        if (currentPrice != null) {
            return currentPrice <= alert.targetPrice
        }

        val normalizedStatus = alert.status?.trim()?.lowercase().orEmpty()
        return (
            alert.targetReached == true ||
                alert.isTargetReached == true ||
                normalizedStatus in reachedStatuses
            )
    }

    fun statusText(alert: PriceAlertResponse): String =
        if (isTargetReached(alert)) "Target reached!" else "Waiting for price drop"

    private val reachedStatuses = setOf(
        "target_reached",
        "target reached",
        "reached",
    )
}

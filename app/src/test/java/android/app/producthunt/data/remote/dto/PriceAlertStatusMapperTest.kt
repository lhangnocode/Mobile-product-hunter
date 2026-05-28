package android.app.producthunt.data.remote.dto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PriceAlertStatusMapperTest {
    @Test
    fun targetReachedWhenCurrentPriceIsBelowTarget() {
        val alert = alert(currentPrice = 90.0, targetPrice = 100.0)

        assertTrue(PriceAlertStatusMapper.isTargetReached(alert))
        assertEquals("Target reached!", PriceAlertStatusMapper.statusText(alert))
    }

    @Test
    fun targetReachedWhenBackendPersistsExplicitReachedFlagWithoutCurrentPrice() {
        val alert = alert(isTargetReached = true, currentPrice = null)

        assertTrue(PriceAlertStatusMapper.isTargetReached(alert))
    }

    @Test
    fun waitingWhenPriceIsAboveTargetEvenIfBackendHasTriggerMarkers() {
        val alert = alert(
            currentPrice = 120.0,
            targetPrice = 100.0,
            isTargetReached = true,
            isTriggered = true,
            lastNotifiedAt = "2026-05-29T09:00:00Z",
        )

        assertFalse(PriceAlertStatusMapper.isTargetReached(alert))
        assertEquals("Waiting for price drop", PriceAlertStatusMapper.statusText(alert))
    }

    @Test
    fun inactiveListedAlertIsNotReachedWithoutPriceOrExplicitReachedState() {
        val alert = alert(isActive = false, currentPrice = null)

        assertFalse(PriceAlertStatusMapper.isTargetReached(alert))
    }

    @Test
    fun waitingWhenPriceIsAboveTargetAndBackendHasNoReachedMarker() {
        val alert = alert(currentPrice = 120.0, targetPrice = 100.0)

        assertFalse(PriceAlertStatusMapper.isTargetReached(alert))
        assertEquals("Waiting for price drop", PriceAlertStatusMapper.statusText(alert))
    }

    @Test
    fun usesFallbackLatestPriceForDisplayAndStatus() {
        val alert = alert(currentPrice = null, latestPrice = 95.0, targetPrice = 100.0)

        assertEquals(95.0, PriceAlertStatusMapper.displayCurrentPrice(alert))
        assertTrue(PriceAlertStatusMapper.isTargetReached(alert))
    }

    private fun alert(
        targetPrice: Double = 100.0,
        currentPrice: Double? = null,
        latestPrice: Double? = null,
        isActive: Boolean = true,
        isTargetReached: Boolean? = null,
        isTriggered: Boolean? = null,
        lastNotifiedAt: String? = null,
    ) = PriceAlertResponse(
        id = "alert-1",
        productId = "product-1",
        userId = "user-1",
        targetPrice = targetPrice,
        currentPrice = currentPrice,
        latestPrice = latestPrice,
        isActive = isActive,
        isTargetReached = isTargetReached,
        isTriggered = isTriggered,
        lastNotifiedAt = lastNotifiedAt,
        createdAt = "2026-05-29T08:00:00Z",
        product = null,
    )
}

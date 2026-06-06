package android.app.producthunt.data.remote.dto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PriceAlertStatusMapperTest {
    @Test
    fun waitingWhenCurrentPriceIsBelowTargetWithoutBackendReachedState() {
        val alert = alert(currentPrice = 90.0, targetPrice = 100.0)

        assertFalse(PriceAlertStatusMapper.isTargetReached(alert))
        assertEquals("Waiting for price drop", PriceAlertStatusMapper.statusText(alert))
    }

    @Test
    fun targetReachedWhenBackendReportsExplicitReachedFlag() {
        val alert = alert(isTargetReached = true, currentPrice = 120.0)

        assertTrue(PriceAlertStatusMapper.isTargetReached(alert))
        assertEquals("Target reached!", PriceAlertStatusMapper.statusText(alert))
    }

    @Test
    fun targetReachedWhenBackendReportsTriggeredMarker() {
        val alert = alert(
            currentPrice = 120.0,
            targetPrice = 100.0,
            isTriggered = true,
            lastNotifiedAt = "2026-05-29T09:00:00Z",
        )

        assertTrue(PriceAlertStatusMapper.isTargetReached(alert))
        assertEquals("Target reached!", PriceAlertStatusMapper.statusText(alert))
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
    fun usesFallbackLatestPriceForDisplayOnly() {
        val alert = alert(currentPrice = null, latestPrice = 95.0, targetPrice = 100.0)

        assertEquals(95.0, PriceAlertStatusMapper.displayCurrentPrice(alert))
        assertFalse(PriceAlertStatusMapper.isTargetReached(alert))
    }

    @Test
    fun targetReachedWhenBackendReportsStatusOne() {
        val alert = alert(currentPrice = 120.0, targetPrice = 100.0, status = "1")

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
        status: String? = null,
    ) = PriceAlertResponse(
        id = "alert-1",
        productId = "product-1",
        platformProductId = "platform-product-1",
        userId = "user-1",
        targetPrice = targetPrice,
        currentPrice = currentPrice,
        latestPrice = latestPrice,
        isActive = isActive,
        status = status,
        isTargetReached = isTargetReached,
        isTriggered = isTriggered,
        lastNotifiedAt = lastNotifiedAt,
        createdAt = "2026-05-29T08:00:00Z",
        product = null,
    )
}

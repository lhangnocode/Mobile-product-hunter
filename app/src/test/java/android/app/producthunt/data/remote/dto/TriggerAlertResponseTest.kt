package android.app.producthunt.data.remote.dto

import org.junit.Assert.assertEquals
import org.junit.Test

class TriggerAlertResponseTest {

    @Test
    fun notificationCountUsesTriggeredCountFirst() {
        val response = TriggerAlertResponse(
            triggeredCount = 2,
            sentCount = 3,
            matchedCount = 4,
        )

        assertEquals(2, response.notificationCount())
    }

    @Test
    fun notificationCountFallsBackToSentCount() {
        val response = TriggerAlertResponse(
            sentCount = 3,
            matchedCount = 4,
        )

        assertEquals(3, response.notificationCount())
    }

    @Test
    fun notificationCountFallsBackToMatchedCount() {
        val response = TriggerAlertResponse(matchedCount = 4)

        assertEquals(4, response.notificationCount())
    }

    @Test
    fun notificationCountDefaultsToZeroAndCoercesNegativeValues() {
        assertEquals(0, TriggerAlertResponse().notificationCount())
        assertEquals(0, TriggerAlertResponse(triggeredCount = -1).notificationCount())
    }
}

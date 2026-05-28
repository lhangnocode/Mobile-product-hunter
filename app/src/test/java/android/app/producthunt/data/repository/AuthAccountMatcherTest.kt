package android.app.producthunt.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthAccountMatcherTest {

    @Test
    fun normalizeEmail_trimsInput() {
        assertEquals("user@example.com", AuthAccountMatcher.normalizeEmail("  user@example.com  "))
    }

    @Test
    fun matchesLoginEmail_ignoresCaseAndWhitespace() {
        assertTrue(AuthAccountMatcher.matchesLoginEmail("  User@Example.com ", "user@example.com"))
    }

    @Test
    fun matchesLoginEmail_rejectsDifferentProfileEmail() {
        assertFalse(AuthAccountMatcher.matchesLoginEmail("user@example.com", "other@example.com"))
    }
}

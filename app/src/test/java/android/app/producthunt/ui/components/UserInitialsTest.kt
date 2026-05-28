package android.app.producthunt.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class UserInitialsTest {

    @Test
    fun from_usesFirstAndLastName() {
        assertEquals("NT", UserInitials.from("Nguyen Van Thang"))
    }

    @Test
    fun from_usesTwoLettersForSingleName() {
        assertEquals("TH", UserInitials.from("Thang"))
    }

    @Test
    fun from_fallsBackToEmail() {
        assertEquals("US", UserInitials.from(null, "user@example.com"))
    }

    @Test
    fun from_fallsBackToDefaultWhenBlank() {
        assertEquals("U", UserInitials.from("", ""))
    }
}

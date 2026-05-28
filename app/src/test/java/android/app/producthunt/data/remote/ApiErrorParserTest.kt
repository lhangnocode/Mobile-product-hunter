package android.app.producthunt.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ApiErrorParserTest {

    @Test
    fun parseMessage_readsFastApiDetailString() {
        val message = ApiErrorParser.parseMessage("""{"detail":"Email hoặc mật khẩu không đúng."}""")

        assertEquals("Email hoặc mật khẩu không đúng.", message)
    }

    @Test
    fun parseMessage_readsMessageString() {
        val message = ApiErrorParser.parseMessage("""{"message":"Reset password email sent successfully"}""")

        assertEquals("Reset password email sent successfully", message)
    }

    @Test
    fun parseMessage_readsFastApiValidationMessage() {
        val body = """{"detail":[{"loc":["body","email"],"msg":"field required","type":"value_error.missing"}]}"""

        assertEquals("field required", ApiErrorParser.parseMessage(body))
    }

    @Test
    fun parseMessage_returnsNullForInvalidJson() {
        assertNull(ApiErrorParser.parseMessage("HTTP 400"))
    }
}

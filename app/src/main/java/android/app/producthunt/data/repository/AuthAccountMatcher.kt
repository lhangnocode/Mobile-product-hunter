package android.app.producthunt.data.repository

internal object AuthAccountMatcher {
    fun normalizeEmail(email: String): String = email.trim()

    fun matchesLoginEmail(loginEmail: String, profileEmail: String): Boolean =
        profileEmail.equals(normalizeEmail(loginEmail), ignoreCase = true)
}

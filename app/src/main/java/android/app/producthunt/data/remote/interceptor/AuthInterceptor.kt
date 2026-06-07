package android.app.producthunt.data.remote.interceptor

import android.app.producthunt.data.local.TokenDataStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

private val NO_AUTH_PATHS = setOf(
    "api/v1/auth/login",
    "api/v1/auth/register",
    "api/v1/auth/refresh",
    "api/v1/agent/",
)

class AuthInterceptor @Inject constructor(
    private val tokenDataStore: TokenDataStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath.trimStart('/')

        if (NO_AUTH_PATHS.any { path.startsWith(it) }) {
            return chain.proceed(request)
        }

        val token = runBlocking { tokenDataStore.getAccessToken() }
        val authed = if (token != null) {
            request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }
        return chain.proceed(authed)
    }
}

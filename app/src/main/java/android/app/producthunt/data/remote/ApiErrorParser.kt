package android.app.producthunt.data.remote

import com.google.gson.JsonParser
import retrofit2.HttpException

internal object ApiErrorParser {
    fun messageFrom(throwable: Throwable, fallback: String): String {
        val httpException = throwable as? HttpException ?: return throwable.message ?: fallback
        val errorBody = httpException.response()?.errorBody()?.string()
        return parseMessage(errorBody) ?: fallback
    }

    fun parseMessage(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null

        return runCatching {
            val root = JsonParser.parseString(errorBody)
            if (!root.isJsonObject) return@runCatching null

            val obj = root.asJsonObject
            obj.get("detail")?.asMessage()
                ?: obj.get("message")?.asMessage()
        }.getOrNull()
    }

    private fun com.google.gson.JsonElement.asMessage(): String? {
        if (isJsonPrimitive && asJsonPrimitive.isString) {
            return asString.takeIf { it.isNotBlank() }
        }

        if (isJsonArray) {
            return asJsonArray
                .mapNotNull { item ->
                    if (item.isJsonObject) {
                        item.asJsonObject.get("msg")?.takeIf { it.isJsonPrimitive }?.asString
                    } else if (item.isJsonPrimitive && item.asJsonPrimitive.isString) {
                        item.asString
                    } else {
                        null
                    }
                }
                .firstOrNull { it.isNotBlank() }
        }

        return null
    }
}

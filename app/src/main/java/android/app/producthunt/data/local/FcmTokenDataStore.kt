package android.app.producthunt.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.fcmDataStore by preferencesDataStore(name = "fcm_prefs")

@Singleton
class FcmTokenDataStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    companion object {
        private val KEY_LAST_TOKEN = stringPreferencesKey("last_fcm_token")
    }

    suspend fun saveLastToken(token: String) {
        context.fcmDataStore.edit { prefs ->
            prefs[KEY_LAST_TOKEN] = token
        }
    }

    suspend fun getLastToken(): String? =
        context.fcmDataStore.data.map { it[KEY_LAST_TOKEN] }.firstOrNull()

    suspend fun clearLastToken() {
        context.fcmDataStore.edit { prefs ->
            prefs.remove(KEY_LAST_TOKEN)
        }
    }
}

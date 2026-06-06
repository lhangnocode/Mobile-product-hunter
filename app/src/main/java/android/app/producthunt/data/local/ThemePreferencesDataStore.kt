package android.app.producthunt.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.themeDataStore by preferencesDataStore(name = "theme_prefs")

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    companion object {
        fun from(value: String?): ThemeMode =
            entries.firstOrNull { it.name == value } ?: SYSTEM
    }
}

enum class LanguageMode {
    ENGLISH,
    VIETNAMESE;

    companion object {
        fun from(value: String?): LanguageMode =
            entries.firstOrNull { it.name == value } ?: VIETNAMESE
    }
}

@Singleton
class ThemePreferencesDataStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    companion object {
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_LANGUAGE_MODE = stringPreferencesKey("language_mode")
        private val KEY_PRICE_ALERT_NOTIFICATIONS_ENABLED =
            booleanPreferencesKey("price_alert_notifications_enabled")
    }

    val themeMode: Flow<ThemeMode> =
        context.themeDataStore.data.map { prefs ->
            ThemeMode.from(prefs[KEY_THEME_MODE])
        }

    val languageMode: Flow<LanguageMode> =
        context.themeDataStore.data.map { prefs ->
            LanguageMode.from(prefs[KEY_LANGUAGE_MODE])
        }

    val priceAlertNotificationsEnabled: Flow<Boolean> =
        context.themeDataStore.data.map { prefs ->
            prefs[KEY_PRICE_ALERT_NOTIFICATIONS_ENABLED] ?: true
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.themeDataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode.name
        }
    }

    suspend fun setLanguageMode(mode: LanguageMode) {
        context.themeDataStore.edit { prefs ->
            prefs[KEY_LANGUAGE_MODE] = mode.name
        }
    }

    suspend fun setPriceAlertNotificationsEnabled(enabled: Boolean) {
        context.themeDataStore.edit { prefs ->
            prefs[KEY_PRICE_ALERT_NOTIFICATIONS_ENABLED] = enabled
        }
    }
}

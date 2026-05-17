package android.app.producthunt.data.local

import android.content.Context
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

@Singleton
class ThemePreferencesDataStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    companion object {
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val themeMode: Flow<ThemeMode> =
        context.themeDataStore.data.map { prefs ->
            ThemeMode.from(prefs[KEY_THEME_MODE])
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.themeDataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode.name
        }
    }
}

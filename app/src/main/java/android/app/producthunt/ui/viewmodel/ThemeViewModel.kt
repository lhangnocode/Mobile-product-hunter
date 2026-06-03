package android.app.producthunt.ui.viewmodel

import android.app.producthunt.data.local.LanguageMode
import android.app.producthunt.data.local.ThemeMode
import android.app.producthunt.data.local.ThemePreferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val themePreferences: ThemePreferencesDataStore,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = themePreferences.themeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ThemeMode.SYSTEM,
    )

    val languageMode: StateFlow<LanguageMode> = themePreferences.languageMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LanguageMode.VIETNAMESE,
    )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themePreferences.setThemeMode(mode)
        }
    }

    fun setDarkMode(enabled: Boolean) {
        setThemeMode(if (enabled) ThemeMode.DARK else ThemeMode.LIGHT)
    }

    fun setLanguageMode(mode: LanguageMode) {
        viewModelScope.launch {
            themePreferences.setLanguageMode(mode)
        }
    }
}

package com.smartoptimize.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.smartoptimize.app.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "smart_optimize_prefs")

/**
 * On-device preferences only: protected ("keep") packages and the Focus
 * Mode switch. Nothing is ever sent off the device — the app does not even
 * declare the INTERNET permission.
 */
class PreferencesRepositoryImpl(context: Context) : PreferencesRepository {

    private val dataStore = context.applicationContext.dataStore

    override val protectedPackages: Flow<Set<String>> =
        dataStore.data.map { it[KEY_PROTECTED] ?: emptySet() }

    override val focusModeEnabled: Flow<Boolean> =
        dataStore.data.map { it[KEY_FOCUS_MODE] ?: false }

    override suspend fun setPackageProtected(packageName: String, isProtected: Boolean) {
        dataStore.edit { prefs ->
            val current = (prefs[KEY_PROTECTED] ?: emptySet()).toMutableSet()
            if (isProtected) current.add(packageName) else current.remove(packageName)
            prefs[KEY_PROTECTED] = current
        }
    }

    override suspend fun setFocusModeEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_FOCUS_MODE] = enabled }
    }

    companion object {
        private val KEY_PROTECTED = stringSetPreferencesKey("protected_packages")
        private val KEY_FOCUS_MODE = booleanPreferencesKey("focus_mode_enabled")
    }
}
